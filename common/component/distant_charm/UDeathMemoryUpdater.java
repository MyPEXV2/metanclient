package relake.common.component.distant_charm;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import relake.common.component.hitaura.UBoxPoints;
import relake.common.component.rotation.FreeLookComponent;
import relake.common.util.MathUtil;
import relake.common.util.StopWatch;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class UDeathMemoryUpdater {
    public final List<EntityDeathMemory> DEATH_MEMORIES_LIST;
    private final long maxTimeMemory;
    private final Supplier<Runnable> memoryDeathTrigger;
    private final Supplier<Boolean> rotateToKilled;
    private LivingEntity lastKilledEntity = null;
    public LivingEntity getLastKilledEntity() {return this.lastKilledEntity;}
    private UDeathMemoryUpdater(long maxTimeMemory, Supplier<Runnable> memoryDeathTrigger, Supplier<Boolean> rotateToKilled) {
        this.DEATH_MEMORIES_LIST = new ArrayList();
        this.maxTimeMemory = maxTimeMemory;
        this.memoryDeathTrigger = memoryDeathTrigger;
        this.rotateToKilled = rotateToKilled;
    }
    public static UDeathMemoryUpdater create(long maxTimeMemory, Supplier<Runnable> memoryDeathTrigger, Supplier<Boolean> rotateToKilled) {
        return new UDeathMemoryUpdater(maxTimeMemory, memoryDeathTrigger, rotateToKilled);
    }

    public void controllingAddingMemoryToEntity(LivingEntity baseTo, boolean mobDetect) {
        if (baseTo == null || baseTo instanceof ClientPlayerEntity || baseTo.ticksExisted < 2 || !baseTo.isAlive()) return;
        EntityDeathMemory searchedMemory = null;
        for (EntityDeathMemory memory : this.DEATH_MEMORIES_LIST) {
            if (!memory.isValidMemory()) continue;
            if (memory.base.getEntityId() == baseTo.getEntityId())
                searchedMemory = memory;
        }
        if (searchedMemory != null) {
            searchedMemory.resetMemory(baseTo, this.maxTimeMemory);
            return;
        }
        if (!mobDetect && !(baseTo instanceof PlayerEntity) || !this.DEATH_MEMORIES_LIST.isEmpty()) return;
        this.DEATH_MEMORIES_LIST.add(new EntityDeathMemory(baseTo, this.maxTimeMemory, memoryDeathTrigger.get()));
    }

    public void removeAutoMemories() {
        this.DEATH_MEMORIES_LIST.removeIf(EntityDeathMemory::isNotValidMemory);
    }

    public void updateAutoMemories() {
        this.DEATH_MEMORIES_LIST.forEach(UDeathMemoryUpdater.EntityDeathMemory::updateMemoryTrigger);
    }
    public void onContains(LivingEntity living) {
        EntityDeathMemory searchedMemory = null;
        for (EntityDeathMemory memory : this.DEATH_MEMORIES_LIST) {
            if (!memory.isValidMemory()) continue;
            if (memory.base.getEntityId() == living.getEntityId()) {
                searchedMemory = memory;
                this.lastKilledEntity = memory.base;
                this.memoryDeathTrigger.get().run();
            }
        }
        if (searchedMemory != null) this.DEATH_MEMORIES_LIST.remove(searchedMemory);
    }

    public class EntityDeathMemory {
        private final StopWatch startTime = new StopWatch();
        private long maxTimeMemory;
        private LivingEntity base;
        private boolean hasReseted;
        private final Runnable onTrigger;

        public EntityDeathMemory(LivingEntity base, long maxTimeMemory, Runnable onTrigger) {
            this.base = base;
            this.startTime.reset();
            this.maxTimeMemory = maxTimeMemory;
            this.onTrigger = onTrigger;
        }

        public void resetMemory(LivingEntity base, long maxTimeMemory) {
            if (this.base != null && this.base.getHealth() == 0) return;
            this.base = base;
            this.maxTimeMemory = maxTimeMemory;
            this.startTime.reset();
        }

        public boolean isValidMemory() {
            return this.base != null && this.maxTimeMemory != 0 && !this.startTime.finished(this.maxTimeMemory);
        }

        public boolean isNotValidMemory() {
            return !isValidMemory();
        }

        public void updateMemoryTrigger() {
            if (this.isValidMemory() && this.base.getHealth() == 0 && !this.hasReseted) {
                lastKilledEntity = this.base;
                this.onTrigger.run();
                if (rotateToKilled.get() && Minecraft.getInstance().player != null) {
                    final Vector2f rotate = UBoxPoints.getVanillaRotate(this.base.getEyePosition(Minecraft.getInstance().getRenderPartialTicks()));
                    FreeLookComponent.setFreeYaw(rotate.x);
                    FreeLookComponent.setFreePitch(MathUtil.lerp(FreeLookComponent.getFreePitch(), rotate.y, .333333F));
                }
                this.hasReseted = true;
                this.maxTimeMemory = 300L;
            }
        }
    }
}
