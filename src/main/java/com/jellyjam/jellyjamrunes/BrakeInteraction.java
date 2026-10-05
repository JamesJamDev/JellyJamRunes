package com.jellyjam.jellyjamrunes;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class BrakeInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<BrakeInteraction> CODEC = BuilderCodec.builder(
            BrakeInteraction.class, BrakeInteraction::new,
            SimpleInstantInteraction.CODEC).build();

    @Override
    protected void firstRun(@Nonnull InteractionType type,
                            @Nonnull InteractionContext ctx,
                            @Nonnull CooldownHandler cooldownHandler) {
        Velocity vel = ctx.getCommandBuffer().getComponent(ctx.getEntity(), Velocity.getComponentType());
        if (vel == null) return;

        Vector3d cur = vel.getClientVelocity();
        // zero horizontal, keep vertical
        vel.addInstruction(new Vector3d(0, cur.y, 0), null, ChangeVelocityType.Set);
    }
}
