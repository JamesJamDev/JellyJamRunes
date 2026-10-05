package com.jellyjam.jellyjamrunes;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

public class TinyEffectWatcher extends EntityTickingSystem<EntityStore> {

    private int tinyIndex = Integer.MIN_VALUE;

    private final Set<Ref<EntityStore>> hadEffect = new HashSet<>();

    private static final Field MOVEMENT_SETTINGS_FIELD;

    static {
        try {
            MOVEMENT_SETTINGS_FIELD =
                    MovementManager.class.getDeclaredField("settings");

            MOVEMENT_SETTINGS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    "Could not access MovementManager.settings", e
            );
        }
    }

    @Override
    public void tick(
            float dt,
            int i,
            @NonNullDecl ArchetypeChunk<EntityStore> chunk,
            @NonNullDecl Store<EntityStore> store,
            @NonNullDecl CommandBuffer<EntityStore> commandBuffer
    ) {

        // Resolve the effect index lazily.
        if (tinyIndex == Integer.MIN_VALUE) {
            tinyIndex =
                    EntityEffect.getAssetMap().getIndex("Tiny_Effect");

            Main.LOGGER.at(Level.INFO)
                    .log("Tiny_Effect index = " + tinyIndex);
        }

        Ref<EntityStore> ref =
                chunk.getReferenceTo(i);

        EffectControllerComponent effects =
                chunk.getComponent(
                        i,
                        EffectControllerComponent.getComponentType()
                );

        if (effects == null) {
            return;
        }

        boolean has =
                effects.hasEffect(tinyIndex);

        if (has && hadEffect.add(ref)) {
            onTinyApplied(ref, store);
        }
        else if (!has && hadEffect.remove(ref)) {
            onTinyRemoved(ref, store, commandBuffer);
        }
    }

    private void onTinyApplied(
            Ref<EntityStore> ref,
            Store<EntityStore> store
    ) {

        MovementManager movement =
                store.getComponent(
                        ref,
                        MovementManager.getComponentType()
                );

        PlayerRef playerRef =
                store.getComponent(
                        ref,
                        PlayerRef.getComponentType()
                );

        if (movement == null || playerRef == null) {
            return;
        }

        /*
         * Start from the player's ACTUAL default movement settings.
         *
         * This is important:
         * - uses the world's configured MovementConfig
         * - preserves mass
         * - preserves dragCoefficient
         * - preserves invertedGravity
         * - preserves all normal falling/air movement values
         */
        MovementSettings settings =
                new MovementSettings(
                        movement.getDefaultSettings()
                );

        /*
         * Tiny only changes these two values.
         */
        settings.baseSpeed *= 0.6f;
        settings.jumpForce *= 0.7f;

        /*
         * Make these settings the active MovementManager settings.
         */
        try {
            MOVEMENT_SETTINGS_FIELD.set(
                    movement,
                    settings
            );
        } catch (IllegalAccessException e) {
            Main.LOGGER.at(Level.SEVERE).log(
                    "Failed to apply Tiny movement settings: " + e.getMessage()
            );
            return;
        }

        /*
         * Send the exact settings to the client.
         */
        movement.update(
                playerRef.getPacketHandler()
        );
    }

    private void onTinyRemoved(
            Ref<EntityStore> ref,
            Store<EntityStore> store,
            CommandBuffer<EntityStore> commandBuffer
    ) {

        MovementManager movement =
                store.getComponent(
                        ref,
                        MovementManager.getComponentType()
                );

        if (movement == null) {
            return;
        }

        /*
         * This does exactly what Hytale itself uses to restore
         * normal movement.
         *
         * It also refreshes the defaults from the world's
         * current PlayerConfig.
         */
        movement.resetDefaultsAndUpdate(
                ref,
                store
        );
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(
                Player.getComponentType(),
                EffectControllerComponent.getComponentType(),
                MovementManager.getComponentType()
        );
    }
}