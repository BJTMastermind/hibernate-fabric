package com.nyakotech.hibernateforge.mixin;

import com.nyakotech.hibernateforge.HibernateForge;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWorld()Z"))
    private boolean redirectTick(Entity entity) {
        if (HibernateForge.isHibernating()) {
            return false; // Skip ticking when hibernating
        }
        return entity.isAddedToWorld();
    }
}
