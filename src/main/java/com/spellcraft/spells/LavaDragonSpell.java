package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class LavaDragonSpell extends AbstractSpell {

    private Location currentLocation;
    private final double damage = 8.0;

    public LavaDragonSpell() {
        super(
                "LavaDragon",
                "Summon a blazing lava dragon that scorches enemies",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.lavadragon.magic-cost", 120),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.lavadragon.cooldown", 15000),
                30.0,
                true,
                "Left Click"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        World world = player.getWorld();
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        world.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 0.6f);

        new BukkitRunnable() {
            double distance = 0;

            @Override
            public void run() {
                if (distance > 20) {
                    cancel();
                    return;
                }

                Location point = start.clone().add(direction.clone().multiply(distance));

                // Dragon body particles
                world.spawnParticle(Particle.FLAME, point, 25, 0.6,0.4,0.6, 0.02);
                world.spawnParticle(Particle.LAVA, point, 8, 0.5,0.3,0.5, 0.01);
                world.spawnParticle(Particle.SMOKE_LARGE, point, 10, 0.4,0.3,0.4, 0.01);

                // Damage nearby enemies
                for (Entity entity : world.getNearbyEntities(point, 2.5, 2.5, 2.5)) {
                    if (entity instanceof Player target && !target.equals(player)) {
                        target.damage(damage, player);
                        target.setFireTicks(100);
                    }
                }

                // Dragon roar along path
                if (distance % 5 == 0) {
                    world.playSound(point, Sound.ENTITY_BLAZE_SHOOT, 0.7f, 0.7f);
                }

                distance += 0.8;
            }
        }.runTaskTimer(SpellCraftPlugin.getInstance(), 0, 1);

        return SpellResult.SUCCESS;
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}
    @Override public boolean isSneakingAbility() { return false; }
    @Override public Action getAbilityActivationAction() { return Action.LEFT_CLICK_AIR; }
    @Override public MagicElement getElement() { return MagicElement.FIRE; }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null
                ? currentLocation.clone()
                : new Location(Bukkit.getWorlds().getFirst(),0,0,0);
    }
}
