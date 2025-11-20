package relake.module.implement.movement;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import relake.Client;
import relake.common.component.rotation.FreeLookComponent;
import relake.common.util.ChatUtil;
import relake.common.util.InventoryUtil;
import relake.common.util.MoveUtil;
import relake.event.EventHandler;
import relake.event.impl.player.MoveEvent;
import relake.event.impl.player.PlayerEvent;
import relake.menu.ui.components.module.setting.SelectComponent;
import relake.module.Module;
import relake.module.ModuleCategory;
import relake.settings.Setting;
import relake.settings.implement.BooleanSetting;
import relake.settings.implement.SelectSetting;

public class SpeedModule extends Module {
    

    public final SelectSetting mode = new SelectSetting("Обход")
            .setValue("Matrix",
                    "FunTime",
                    "FunTime extra",
                    "HolyWorld");
    
    public SpeedModule() {
        super("Speed", "Увеличивает скорость передвижения по ландшафту", "Increases the speed of movement across the landscape", ModuleCategory.Movement);
        registerComponent(mode);
        mode.setSelected("FunTime extra");
    }

    private boolean wasTimer, wasJumping;

    @EventHandler
    public void onMove(MoveEvent event) {
        if (lastSendedSneak && mode.isSelected("FunTime extra") && !mc.player.movementInput.jump) {
            int speedLvl = mc.player.isPotionActive(Effects.SPEED) ? mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1 : 0;
            MoveUtil.setSpeed(event, mc.player.getAIMoveSpeed() / mc.player.landMovementFactor * 1.3F * (mc.player.movementInput.jump ? .32F : .269F) + (.021F * speedLvl));
        }
    }

    @EventHandler
    public void player(PlayerEvent event) {
        if (mode.isSelected("Matrix")) handleMatrix();
        if (mode.isSelected("FunTime")) handleFunTime();
        if (mode.isSelected("FunTime extra")) handleFunTimeExtra(event);
        if (mode.isSelected("HolyWorld")) handleHolyWorld(event);
    }

    private void handleMatrix() {
        if (wasTimer) {
            wasTimer = false;
            mc.timer.speed = 1.0f;
        }
        mc.player.motion.y -= 0.00348;
        mc.player.jumpMovementFactor = 0.026f;
        mc.gameSettings.keyBindJump.pressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), mc.gameSettings.keyBindJump.keyCode.getKeyCode());
        if (MoveUtil.isMoving() && mc.player.isOnGround()) {
            mc.gameSettings.keyBindJump.pressed = false;
            mc.timer.speed = 1.35f;
            wasTimer = true;
            mc.player.jump();
            MoveUtil.strafe();
        } else if (MoveUtil.getSpeed() < 0.215) {
            MoveUtil.strafe(0.215f);
        }
    }

    private void handleFunTime() {
        BlockPos playerPos = new BlockPos(mc.player.getPositionVec());
        BlockState blockState = mc.world.getBlockState(playerPos);
        if ((blockState.getBlock() instanceof SlabBlock || blockState.getBlock() instanceof StairsBlock) && mc.player.fallDistance == 0 && MoveUtil.isMoving()) {
            if (mc.player.isOnGround()) {
                mc.player.motion.y += 0.1F;

                if (mc.player.isSprinting()) {
                    float s = 0.05F;
                    float f1 = mc.player.rotationYaw * ((float) Math.PI / 180F);
                    mc.player.setMotion(mc.player.getMotion().add(-MathHelper.sin(f1) * s, 0.0D, MathHelper.cos(f1) * s));
                }

                mc.player.isAirBorne = true;
            }
        }
    }
    public boolean lastSendedSneak;
    public boolean isBlockUnderWithMotion() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset((mc.player.getPosX() - mc.player.lastTickPosX) * 5.F, -0.1, (mc.player.getPosZ() - mc.player.lastTickPosZ) * 5.F);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
    private void handleFunTimeExtra(PlayerEvent event) {
        if ((mc.player.isOnGround() && mc.player.movementInput.jump || mc.player.ticksOnGround > 1) && MoveUtil.isMoving() && !this.isBlockUnderWithMotion()) {
            final AxisAlignedBB boundingBox = mc.player.getBoundingBox().expand(0.5, -0.51, 0.5);
            int minX = MathHelper.floor(boundingBox.minX), minY = MathHelper.floor(boundingBox.minY), minZ = MathHelper.floor(boundingBox.minZ);
            int maxX = MathHelper.floor(boundingBox.maxX), maxY = MathHelper.floor(boundingBox.maxY), maxZ = MathHelper.floor(boundingBox.maxZ);
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        if (!mc.world.getBlockState(pos).isAir())
                            new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP).send();
                    }
                }
            }
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY).send();
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY).send();
            lastSendedSneak = true;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
               mc.player.getMotion().x *= 1.1515F;
               mc.player.getMotion().z *= 1.1515F;
            } else {
                mc.player.setOnGround(false);
                MoveUtil.setSpeed(.0D);
                event.setOnGround(true);
            }
        } else if (lastSendedSneak) {
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY).send();
            lastSendedSneak = false;
        }
    }

    private void handleHolyWorld(PlayerEvent event) {
        if (mc.player.isInWater()) {
            return;
        }

        if (mc.player.isOnGround()) {
            if (!wasJumping) {
                wasJumping = true;
                placeIceBlock();
                event.setRotate(new Vector2f(mc.player.rotationYaw, 69));
            }
        } else {
            wasJumping = false;
            event.setRotate(new Vector2f(mc.player.rotationYaw, 69));
        }
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private void placeIceBlock() {
        BlockPos blockPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY() - 0.6, mc.player.getPosZ());

        if (mc.world.getBlockState(blockPos).isAir()) {
            return;
        }

        int iceSlot = InventoryUtil.findIceInHotBar();

        if (iceSlot == -1) {
            ChatUtil.send("Не нашел блоки льда в хотбаре!");
            switchState();
            return;
        }

        mc.player.inventory.currentItem = iceSlot;
        mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, blockPos, Direction.UP));

        Vector3d blockCenter = new Vector3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
        mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(blockCenter, Direction.UP, blockPos, false)));
        mc.world.setBlockState(blockPos, Blocks.BLUE_ICE.getDefaultState());
    }

    @Override
    public void disable() {
        super.disable();
        wasTimer = false;
        mc.timer.speed = 1.f;
        if (lastSendedSneak) {
            new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY).send();
            lastSendedSneak = false;
        }
    }
}
