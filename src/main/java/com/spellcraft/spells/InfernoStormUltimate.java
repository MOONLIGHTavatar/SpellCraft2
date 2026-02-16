package com.spellcraft.spells.ultimate;

import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.*;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

public class InfernoStormUltimate extends AbstractSpell {

    private Location currentLocation;

    public InfernoStormUltimate() {
        super(
                "InfernoStorm",
                "Unleash the fury of fire, lava, and lightning",
                SpellCategory.ULTIMATE,
                SpellCraftPlugin.getInstance().getConfig().getInt("ultimates.infernostorm.magic-cost", 100),
                SpellCraftPlugin.getInstance().getConfig().getLong("ultimates.infernostorm.cooldown", 30000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("ultimates.infernostorm.range", 15),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("ultimates.enabled", true),
                "Right Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        if (!isEnabled()) return SpellResult.FAILURE;

        currentLocation = caster.getPlayer().getLocation().clone();

        // Spawn particles and effects asynchronously
        ThreadUtil.runAsync(() -> {
            for (int i = 0; i < 50; i++) {
                double offsetX = Math.random() * 10 - 5;
                double offsetZ = Math.random() * 10 - 5;
                Location loc = currentLocation.clone().add(offsetX, 1, offsetZ);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 5);
                loc.getWorld().spawnParticle(Particle.LAVA, loc, 2);
            }

            // Damage nearby entities
            for (Entity e : currentLocation.getWorld().getNearbyEntities(currentLocation, 8, 4, 8)) {
                if (e instanceof LivingEntity target && target != caster.getPlayer()) {
                    target.damage(10 + caster.getMagicPower() * 0.3, caster.getPlayer());
                }
            }

            // Lightning
            for (int i = 0; i < 3; i++) {
                double offsetX = Math.random() * 10 - 5;
                double offsetZ = Math.random() * 10 - 5;
                currentLocation.getWorld().strikeLightning(currentLocation.clone().add(offsetX, 0, offsetZ));
            }

            currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_BLAZE_HURT, 2, 1);
        });

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() { return false; }

    @Override
    public Action getAbilityActivationAction() { return Action.RIGHT_CLICK_AIR; }

    @Override
    public MagicElement getElement() { return MagicElement.FIRE; }

    @Override
    public Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : casterDefaultLocation();
    }

    private Location casterDefaultLocation() {
        return new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
