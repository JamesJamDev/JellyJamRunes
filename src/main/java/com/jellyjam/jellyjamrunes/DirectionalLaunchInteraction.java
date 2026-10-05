package com.jellyjam.jellyjamrunes;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class DirectionalLaunchInteraction extends SimpleInstantInteraction {

    public static final BuilderCodec<DirectionalLaunchInteraction> CODEC = BuilderCodec.builder(
            DirectionalLaunchInteraction.class,
            DirectionalLaunchInteraction::new,
            SimpleInstantInteraction.CODEC).build();

    private static final double FORCE = 30;          // same as your JSON
    private static final double UP = 4;              // roughly your Y 0.3 share
    private static final double MIN_MOVE_SPEED = 0.5;  // below this, treat as standing still

    @Override
    protected void firstRun(@Nonnull InteractionType type,
                            @Nonnull InteractionContext ctx,
                            @Nonnull CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = ctx.getEntity();
        CommandBuffer<EntityStore> cb = ctx.getCommandBuffer();

        Velocity vel = cb.getComponent(ref, Velocity.getComponentType());
        if (vel == null) return;

        Vector3d cur = vel.getClientVelocity();   // last known client velocity
        double len = Math.sqrt(cur.x * cur.x + cur.z * cur.z);
        if (len < MIN_MOVE_SPEED) {
            return;                                // fallback goes here, see below
        }

        double dx = cur.x / len;
        double dz = cur.z / len;
        vel.addInstruction(new Vector3d(dx * FORCE, UP, dz * FORCE), null, ChangeVelocityType.Set);
    }
}