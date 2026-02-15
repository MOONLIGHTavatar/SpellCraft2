package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class DragonBreathSpell extends AbstractSpell {

    private Location currentLocation;

    public DragonBreathSpell() {
        super(
                "DragonBreath",
                "Unleash a fiery dragon breath, dealing soul fire and normal fire around you",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.dragonbreath.magic-cost", 40),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.dragonbreath.cooldown", 8000),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.dragonbreath.range", 5.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.dragonbreath.enabled", true),
                "Right Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        double radius = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.dragonbreath.range", 5.0);
        double damage = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.dragonbreath.damage", 6.0);

        // Particle settings from config
        Particle mainParticle = Particle.valueOf(
                SpellCraftPlugin.getInstance().getConfig().getString("spells.dragonbreath.particles.type-main", "FLAME")
        );
        Particle secondaryParticle = Particle.valueOf(
                SpellCraftPlugin.getInstance().getConfig().getString("spells.dragonbreath.particles.type-secondary", "SOUL_FIRE_FLAME")
        );
        int particleCount = SpellCraftPlugin.getInstance().getConfig().getInt("spells.dragonbreath.particles.count", 10);
        double particleRadius = SpellCraftPlugin.getInstance().getConfig().getDouble("spells.dragonbreath.particles.radius", 2.5);
        int durationTicks = SpellCraftPlugin.getInstance().getConfig().getInt("spells.dragonbreath.particles.duration-ticks", 40);

        // Use SpellCraft ThreadUtil for thread-safe repeating task
        ThreadUtil.ensureLocationTimer(currentLocation, () -> {
            // Spawn particles around player
            for (double angle = 0; angle < 360; angle += 360.0 / particleCount) {
                double radians = Math.toRadians(angle);
                double x = Math.cos(radians) * particleRadius;
                double z = Math.sin(radians) * particleRadius;
                Location particleLoc = currentLocation.clone().add(x, 1.0, z);
                currentLocation.getWorld().spawnParticle(mainParticle, particleLoc, 1, 0, 0, 0, 0.05);
                currentLocation.getWorld().spawnParticle(secondaryParticle, particleLoc, 1, 0, 0, 0, 0.05);

                // Ignite ground blocks
                Location blockLoc = particleLoc.clone();
                blockLoc.setY(currentLocation.getY());
                if (blockLoc.getBlock().getType() == Material.AIR) {
                    blockLoc.getBlock().setType(Material.FIRE);
                }
            }

            // Damage nearby entities
            for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, radius, 2, radius)) {
                if (entity instanceof LivingEntity living && !living.equals(player)) {
                    living.setFireTicks(80); // 4 seconds fire
                    living.damage(damage, player);
                }
            }
        }, 0L, durationTicks / 2L); // Runs durationTicks/2 times

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() {
        return false;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.RIGHT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.FIRE;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
