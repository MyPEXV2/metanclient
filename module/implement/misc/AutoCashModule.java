package relake.module.implement.misc;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.state.IntegerProperty;
import relake.common.component.rotation.Rotation;
import relake.common.component.rotation.RotationComponent;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import relake.common.util.ChatUtil;
import relake.common.util.InventoryUtil;
import relake.common.util.MoveUtil;
import relake.common.util.StopWatch;
import relake.event.EventHandler;
import relake.event.impl.misc.TickEvent;
import relake.event.impl.player.MoveEvent;
import relake.event.impl.player.MovementInputEvent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;


public class AutoCashModule extends Module {
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;
    private BlockPos currentTarget = null;
    private BlockPos currentDestination = null;
    private boolean movingToPos2 = true;
    private State currentState = State.MOVING;
    private final StopWatch actionDelay = new StopWatch();
    private final StopWatch moveDelay = new StopWatch();
    private final StopWatch buyerDelay = new StopWatch();
    private final StopWatch checkDelay = new StopWatch();
    private Vector3d lastPosition = null;
    private int stuckTicks = 0;
    private boolean isStuck = false;
    private BlockPos escapeTarget = null;
    
    // Плавные ротации
    private float currentYaw = 0.0F;
    private float currentPitch = 0.0F;
    private float targetYaw = 0.0F;
    private float targetPitch = 0.0F;
    private boolean hasRotationTarget = false;
    
    // Легит прыжки
    private final StopWatch randomJumpTimer = new StopWatch();
    private long nextJumpTime = 0;
    private final java.util.Random random = new java.util.Random();
    
    private final Setting<Boolean> autoSell = new BooleanSetting("Автопродажа при полном инвентаре")
            .setValue(true);
    
    private static final IntegerProperty AGE_PROPERTY = BlockStateProperties.AGE_0_2;
    
    private enum State {
        MOVING,
        BREAKING,
        PLANTING,
        SELLING
    }
    
    public AutoCashModule() {
        super("Auto Cash", "Автоматически собирает и засеивает какао-бобы", "Automatically collects and replants cocoa beans", ModuleCategory.Misc);
        registerComponent(autoSell);
        scheduleNextJump();
    }
    
    private void scheduleNextJump() {
        // Рандомное время от 2 до 8 минут (120000-480000 мс)
        nextJumpTime = 120000 + random.nextInt(360000);
        randomJumpTimer.reset();
    }
    
    public void setPos1(int x, int y, int z) {
        pos1 = new BlockPos(x, y, z);
    }
    
    public void setPos2(int x, int y, int z) {
        pos2 = new BlockPos(x, y, z);
    }
    
    public void setPos1FromPlayer() {
        if (mc.player != null) {
            pos1 = mc.player.getPosition();
        }
    }
    
    public void setPos2FromPlayer() {
        if (mc.player != null) {
            pos2 = mc.player.getPosition();
        }
    }
    
    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (pos1 == null || pos2 == null) return;
        
        if (!hasRequiredItems()) {
            return;
        }
        
        if (mc.player.inventory.getFirstEmptyStack() == -1 && autoSell.getValue() && currentState != State.SELLING) {
            currentState = State.SELLING;
            return;
        }
        
        checkStuck();
        updateSmoothRotations();
        handleRandomJump();
        
        switch (currentState) {
            case MOVING:
                checkAndMove();
                break;
            case BREAKING:
                breakCocoa();
                break;
            case PLANTING:
                plantCocoa();
                break;
            case SELLING:
                sellToBuyer();
                break;
        }
    }
    
    private void checkStuck() {
        if (currentState != State.MOVING) {
            lastPosition = null;
            stuckTicks = 0;
            isStuck = false;
            escapeTarget = null;
            return;
        }
        
        Vector3d currentPos = mc.player.getPositionVec();
        
        if (lastPosition != null) {
            double distance = currentPos.distanceTo(lastPosition);
            if (distance < 0.1) {
                stuckTicks++;
                if (stuckTicks > 10) {
                    isStuck = true;
                    if (escapeTarget == null && currentDestination != null) {
                        calculateEscapeTarget();
                    }
                }
            } else {
                stuckTicks = 0;
                if (isStuck && escapeTarget != null) {
                    double escapeDistance = currentPos.distanceTo(new Vector3d(escapeTarget.getX() + 0.5, escapeTarget.getY(), escapeTarget.getZ() + 0.5));
                    if (escapeDistance < 2.0) {
                        isStuck = false;
                        escapeTarget = null;
                    }
                } else {
                    isStuck = false;
                    escapeTarget = null;
                }
            }
        }
        
        lastPosition = currentPos;
    }
    
    private void updateSmoothRotations() {
        if (!hasRotationTarget) {
            return;
        }
        
        // Плавная интерполяция поворотов для легитности
        float yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float pitchDiff = targetPitch - currentPitch;
        
        // Скорость поворота: от 15 до 25 градусов за тик (легит скорость)
        float rotationSpeed = 15.0F + random.nextFloat() * 10.0F;
        
        float maxYawChange = Math.min(Math.abs(yawDiff), rotationSpeed);
        float maxPitchChange = Math.min(Math.abs(pitchDiff), rotationSpeed);
        
        if (Math.abs(yawDiff) > 0.5F) {
            currentYaw += Math.signum(yawDiff) * maxYawChange;
            currentYaw = MathHelper.wrapDegrees(currentYaw);
        } else {
            currentYaw = targetYaw;
        }
        
        if (Math.abs(pitchDiff) > 0.5F) {
            currentPitch += Math.signum(pitchDiff) * maxPitchChange;
            currentPitch = MathHelper.clamp(currentPitch, -90.0F, 90.0F);
        } else {
            currentPitch = targetPitch;
        }
        
        // Применяем плавную ротацию
        RotationComponent.update(new Rotation(currentYaw, currentPitch), rotationSpeed, rotationSpeed, 1, 2);
        
        // Проверяем достигли ли цели
        if (Math.abs(yawDiff) < 1.0F && Math.abs(pitchDiff) < 1.0F) {
            hasRotationTarget = false;
        }
    }
    
    private void setRotationTarget(float yaw, float pitch) {
        if (!hasRotationTarget) {
            currentYaw = mc.player.rotationYaw;
            currentPitch = mc.player.rotationPitch;
        }
        targetYaw = yaw;
        targetPitch = pitch;
        hasRotationTarget = true;
    }
    
    private boolean isRotationReady() {
        if (!hasRotationTarget) return true;
        
        float yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - currentYaw));
        float pitchDiff = Math.abs(targetPitch - currentPitch);
        
        // Проверяем что ротация достаточно близка (точность 3 градуса)
        return yawDiff < 3.0F && pitchDiff < 3.0F;
    }
    
    private void handleRandomJump() {
        if (currentState != State.MOVING) return;
        
        if (randomJumpTimer.finished(nextJumpTime)) {
            if (mc.player.isOnGround() && !isStuck) {
                mc.player.jump();
            }
            scheduleNextJump();
        }
    }
    
    private void calculateEscapeTarget() {
        if (currentDestination == null) return;
        
        Vector3d playerPos = mc.player.getPositionVec();
        Vector3d destVec = new Vector3d(currentDestination.getX() + 0.5, currentDestination.getY(), currentDestination.getZ() + 0.5);
        Vector3d direction = destVec.subtract(playerPos);
        
        if (direction.length() < 0.1) return;
        
        direction = direction.normalize();
        float movementYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        
        Direction forwardDir = Direction.fromAngle(movementYaw);
        Direction sideDir = forwardDir.rotateY();
        
        BlockPos playerBlockPos = mc.player.getPosition();
        BlockPos escapePos = playerBlockPos.offset(sideDir, 2);
        
        for (int y = -1; y <= 2; y++) {
            BlockPos checkPos = escapePos.up(y);
            BlockState state = mc.world.getBlockState(checkPos);
            BlockState stateAbove = mc.world.getBlockState(checkPos.up());
            
            if (state.isAir() && stateAbove.isAir()) {
                escapeTarget = checkPos;
                return;
            }
        }
        
        escapeTarget = escapePos;
    }
    
    @EventHandler
    public void onMovementInput(MovementInputEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (pos1 == null || pos2 == null) return;
        
        if (currentState == State.MOVING) {
            if (currentDestination == null) {
                updateDestination();
            }
            if (currentDestination != null) {
                Vector3d targetVec = new Vector3d(currentDestination.getX() + 0.5, currentDestination.getY(), currentDestination.getZ() + 0.5);
                Vector3d playerVec = mc.player.getPositionVec();
                Vector3d direction = targetVec.subtract(playerVec);
                double directionLength = direction.length();
                
                if (directionLength > 0.5) {
                    Vector3d targetVec2;
                    if (isStuck && escapeTarget != null) {
                        targetVec2 = new Vector3d(escapeTarget.getX() + 0.5, escapeTarget.getY(), escapeTarget.getZ() + 0.5);
                    } else {
                        targetVec2 = targetVec;
                    }
                    
                    Vector3d direction2 = targetVec2.subtract(playerVec);
                    float targetYaw = (float) Math.toDegrees(Math.atan2(direction2.z, direction2.x)) - 90.0F;
                    
                    if (isStuck) {
                        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.rotationYaw);
                        
                        if (Math.abs(yawDiff) > 5.0F) {
                            setRotationTarget(targetYaw, mc.player.rotationPitch);
                        }
                        
                        event.setForward(0.7F);
                        event.setStrafe(0.0F);
                    } else {
                        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.rotationYaw);
                        
                        if (Math.abs(yawDiff) > 5.0F) {
                            setRotationTarget(targetYaw, mc.player.rotationPitch);
                        }
                        
                        event.setForward(1.0F);
                        event.setStrafe(0.0F);
                    }
                    
                    if (!mc.player.isSprinting() && mc.player.isOnGround() && !isStuck) {
                        mc.player.setSprinting(true);
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_SPRINTING));
                    }
                } else {
                    event.setForward(0.0F);
                    event.setStrafe(0.0F);
                }
            }
        } else {
            event.setForward(0.0F);
            event.setStrafe(0.0F);
        }
    }
    
    @EventHandler
    public void onMove(MoveEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (pos1 == null || pos2 == null) return;
        if (currentState != State.MOVING) return;
        
        Vector3d targetVec;
        if (isStuck && escapeTarget != null) {
            targetVec = new Vector3d(escapeTarget.getX() + 0.5, escapeTarget.getY(), escapeTarget.getZ() + 0.5);
        } else if (currentDestination != null) {
            targetVec = new Vector3d(currentDestination.getX() + 0.5, currentDestination.getY(), currentDestination.getZ() + 0.5);
        } else {
            return;
        }
        
        Vector3d playerVec = mc.player.getPositionVec();
        Vector3d direction = targetVec.subtract(playerVec);
        double directionLength = direction.length();
        
        if (directionLength > 0.5) {
            direction = direction.normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
            
            if (isStuck && mc.player.isOnGround() && actionDelay.finished(100)) {
                event.getMotion().y = 0.42F;
                actionDelay.reset();
            }
            
            double speed = mc.player.isSprinting() ? 0.28 : 0.22;
            if (isStuck) {
                speed = 0.2;
            }
            
            double cos = Math.cos(Math.toRadians(targetYaw));
            double sin = Math.sin(Math.toRadians(targetYaw));
            
            event.getMotion().x = -sin * speed;
            event.getMotion().z = cos * speed;
        }
    }
    
    private boolean hasRequiredItems() {
        int cocoaSlot = InventoryUtil.getHotBarItemSlot(Items.COCOA_BEANS);
        int axeSlot = findAxeSlot();
        
        if (cocoaSlot == -1 || axeSlot == -1) {
            return false;
        }
        
        // Держим топор в руке по умолчанию
        if (mc.player.inventory.currentItem != axeSlot) {
            mc.player.inventory.currentItem = axeSlot;
        }
        
        return true;
    }
    
    private int findAxeSlot() {
        Item[] axes = {Items.NETHERITE_AXE, Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.WOODEN_AXE};
        for (Item axe : axes) {
            int slot = InventoryUtil.getHotBarItemSlot(axe);
            if (slot != -1) return slot;
        }
        return -1;
    }
    
    private void checkAndMove() {
        if (currentDestination == null) {
            updateDestination();
        }
        
        if (checkDelay.finished(20)) {
            BlockPos playerPos = mc.player.getPosition();
            BlockPos leftPos = checkSideBlock(playerPos, true);
            BlockPos rightPos = checkSideBlock(playerPos, false);
            
            if (leftPos != null) {
                currentTarget = leftPos;
                currentState = State.BREAKING;
                actionDelay.reset();
                return;
            }
            
            if (rightPos != null) {
                currentTarget = rightPos;
                currentState = State.BREAKING;
                actionDelay.reset();
                return;
            }
            
            checkDelay.reset();
        }
        
        if (currentDestination != null) {
            double distance = mc.player.getDistanceSq(currentDestination.getX(), currentDestination.getY(), currentDestination.getZ());
            if (distance < 4.0) {
                movingToPos2 = !movingToPos2;
                currentDestination = null;
                updateDestination();
                isStuck = false;
                stuckTicks = 0;
            }
        }
        
        if (isStuck && stuckTicks > 50) {
            isStuck = false;
            stuckTicks = 0;
            lastPosition = null;
            escapeTarget = null;
        }
        
    }
    
    private void updateDestination() {
        if (movingToPos2) {
            currentDestination = pos2;
        } else {
            currentDestination = pos1;
        }
    }
    
    private BlockPos checkSideBlock(BlockPos playerPos, boolean left) {
        if (currentDestination == null) {
            return null;
        }
        
        Vector3d playerVec = new Vector3d(playerPos.getX(), playerPos.getY(), playerPos.getZ());
        Vector3d destVec = new Vector3d(currentDestination.getX(), currentDestination.getY(), currentDestination.getZ());
        Vector3d direction = destVec.subtract(playerVec);
        
        if (direction.length() < 0.1) {
            return null;
        }
        
        direction = direction.normalize();
        float movementYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
        
        Direction forwardDir = Direction.fromAngle(movementYaw);
        Direction sideDir = left ? forwardDir.rotateY() : forwardDir.rotateYCCW();
        
        for (int offset = 0; offset <= 2; offset++) {
            for (int height = -1; height <= 1; height++) {
                BlockPos checkPos = playerPos.offset(sideDir, offset + 1).up(height);
                
                if (checkPos.getY() < 0 || checkPos.getY() > 255) continue;
                
                BlockState state = mc.world.getBlockState(checkPos);
                Block block = state.getBlock();
                
                if (block == Blocks.COCOA) {
                    try {
                        int age = state.get(AGE_PROPERTY);
                        if (age >= 2) {
                            double distance = mc.player.getDistanceSq(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                            if (distance < 25.0) {
                                return checkPos;
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        
        return null;
    }
    
    private void breakCocoa() {
        if (currentTarget == null) {
            currentState = State.MOVING;
            return;
        }
        
        BlockState state = mc.world.getBlockState(currentTarget);
        if (state.getBlock() != Blocks.COCOA) {
            currentState = State.MOVING;
            currentTarget = null;
            return;
        }
        
        Vector3d targetVec = new Vector3d(currentTarget.getX() + 0.5, currentTarget.getY() + 0.5, currentTarget.getZ() + 0.5);
        Vector3d playerVec = mc.player.getEyePosition(1.0F);
        Vector3d direction = targetVec.subtract(playerVec);
        double directionLength = direction.length();
        
        if (directionLength > 0.1) {
            direction = direction.normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
            float targetPitch = (float) -Math.toDegrees(Math.asin(direction.y / directionLength));
            
            setRotationTarget(targetYaw, targetPitch);
            
            if (!isRotationReady()) {
                return;
            }
        }
        
        if (actionDelay.finished(80)) {
            Direction facing = state.get(BlockStateProperties.HORIZONTAL_FACING);
            mc.player.connection.sendPacket(new CPlayerDiggingPacket(
                    CPlayerDiggingPacket.Action.START_DESTROY_BLOCK,
                    currentTarget,
                    facing.getOpposite()
            ));
            
            mc.player.swingArm(Hand.MAIN_HAND);
            
            actionDelay.reset();
        }
        
        BlockState newState = mc.world.getBlockState(currentTarget);
        if (newState.getBlock() != Blocks.COCOA) {
            currentState = State.PLANTING;
            actionDelay.reset();
        }
    }
    
    private void plantCocoa() {
        if (currentTarget == null) {
            currentState = State.MOVING;
            return;
        }
        
        BlockState state = mc.world.getBlockState(currentTarget);
        if (state.getBlock() == Blocks.COCOA) {
            currentState = State.MOVING;
            currentTarget = null;
            return;
        }
        
        BlockPos logPos = findAdjacentLog(currentTarget);
        if (logPos == null) {
            currentState = State.MOVING;
            currentTarget = null;
            return;
        }
        
        Direction facing = getFacingFromLog(logPos, currentTarget);
        if (facing == null) {
            currentState = State.MOVING;
            currentTarget = null;
            return;
        }
        
        // Целимся на сам лог
        Vector3d hitVec = new Vector3d(
                logPos.getX() + 0.5,
                logPos.getY() + 0.5,
                logPos.getZ() + 0.5
        );
        Vector3d playerVec = mc.player.getEyePosition(1.0F);
        Vector3d direction = hitVec.subtract(playerVec);
        double directionLength = direction.length();
        
        if (directionLength > 0.1) {
            direction = direction.normalize();
            float targetYaw = (float) Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0F;
            float targetPitch = (float) -Math.toDegrees(Math.asin(direction.y / directionLength));
            
            setRotationTarget(targetYaw, targetPitch);
            
            if (!isRotationReady()) {
                return;
            }
        }
        
        if (actionDelay.finished(200)) {
            int cocoaSlot = InventoryUtil.getHotBarItemSlot(Items.COCOA_BEANS);
            if (cocoaSlot == -1) {
                currentState = State.MOVING;
                currentTarget = null;
                return;
            }
            
            int previousSlot = mc.player.inventory.currentItem;
            
            // 1. Отправляем пакет смены слота на какао
            mc.player.connection.sendPacket(new CHeldItemChangePacket(cocoaSlot));
            mc.player.inventory.currentItem = cocoaSlot;
            
            // 2. Небольшая задержка для синхронизации
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
            
            // 3. Вычисляем точку клика
            Vector3d hitPos = new Vector3d(
                    logPos.getX() + 0.5 + (facing.getXOffset() * 0.5),
                    logPos.getY() + 0.5,
                    logPos.getZ() + 0.5 + (facing.getZOffset() * 0.5)
            );
            
            BlockRayTraceResult rayTrace = new BlockRayTraceResult(
                    hitPos,
                    facing,
                    logPos,
                    false
            );
            
            // 4. Отправляем пакет правого клика по блоку
            mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, rayTrace));
            
            // 5. Отправляем пакет анимации руки
            mc.player.connection.sendPacket(new CAnimateHandPacket(Hand.MAIN_HAND));
            
            // 6. Возвращаемся на топор
            int axeSlot = findAxeSlot();
            if (axeSlot != -1) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(axeSlot));
                mc.player.inventory.currentItem = axeSlot;
            }
            
            actionDelay.reset();
            currentState = State.MOVING;
            currentTarget = null;
        }
    }
    
    private BlockPos findAdjacentLog(BlockPos cocoaPos) {
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == Direction.Axis.Y) continue;
            BlockPos checkPos = cocoaPos.offset(dir);
            BlockState state = mc.world.getBlockState(checkPos);
            if (state.getBlock() == Blocks.JUNGLE_LOG || state.getBlock() == Blocks.JUNGLE_WOOD) {
                return checkPos;
            }
        }
        return null;
    }
    
    private Direction getFacingFromLog(BlockPos logPos, BlockPos cocoaPos) {
        int dx = cocoaPos.getX() - logPos.getX();
        int dz = cocoaPos.getZ() - logPos.getZ();
        
        if (dx > 0) return Direction.EAST;
        if (dx < 0) return Direction.WEST;
        if (dz > 0) return Direction.SOUTH;
        if (dz < 0) return Direction.NORTH;
        
        return Direction.NORTH;
    }
    
    private void sellToBuyer() {
        if (mc.player.inventory.getFirstEmptyStack() != -1) {
            currentState = State.MOVING;
            return;
        }
        
        if (mc.currentScreen instanceof ContainerScreen<?> screen) {
            if (screen.getTitle().getString().equals("● Выберите секцию")) {
                if (buyerDelay.finished(200)) {
                    InventoryUtil.clickSlotId(21, 0, ClickType.PICKUP, true);
                    buyerDelay.reset();
                }
                return;
            }
            
            if (screen.getTitle().getString().contains("Скупщик")) {
                if (buyerDelay.finished(200)) {
                    Slot cactusSlot = findCactusSlot(screen);
                    if (cactusSlot != null) {
                        InventoryUtil.clickSlot(cactusSlot, 0, ClickType.PICKUP, true);
                        buyerDelay.reset();
                        return;
                    }
                    
                    Slot cocoaSlot = findCocoaSlot(screen);
                    if (cocoaSlot != null) {
                        InventoryUtil.clickSlot(cocoaSlot, 0, ClickType.QUICK_MOVE, true);
                        buyerDelay.reset();
                    }
                }
                return;
            }
        }
        
        if (buyerDelay.finished(1000)) {
            mc.player.sendChatMessage("/buyer");
            buyerDelay.reset();
        }
    }
    
    private Slot findCactusSlot(ContainerScreen<?> screen) {
        return screen.getContainer().inventorySlots.stream()
                .filter(s -> s.getStack().getItem() == Items.CACTUS && s.slotNumber < 45)
                .findFirst()
                .orElse(null);
    }
    
    private Slot findCocoaSlot(ContainerScreen<?> screen) {
        return screen.getContainer().inventorySlots.stream()
                .filter(s -> s.getStack().getItem() == Items.COCOA_BEANS && s.slotNumber < 45)
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public void enable() {
        super.enable();
        currentYaw = mc.player.rotationYaw;
        currentPitch = mc.player.rotationPitch;
        hasRotationTarget = false;
        scheduleNextJump();
    }
    
    @Override
    public void disable() {
        super.disable();
        pos1 = null;
        pos2 = null;
        currentTarget = null;
        currentDestination = null;
        movingToPos2 = true;
        currentState = State.MOVING;
        actionDelay.reset();
        moveDelay.reset();
        buyerDelay.reset();
        checkDelay.reset();
        randomJumpTimer.reset();
        lastPosition = null;
        stuckTicks = 0;
        isStuck = false;
        escapeTarget = null;
        hasRotationTarget = false;
    }
}



