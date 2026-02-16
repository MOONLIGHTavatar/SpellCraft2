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
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ShadowTentaclesSpell extends AbstractSpell {

    private Location currentLocation;
    private final double range = 16;
    private final double damage = 6.0;

    public ShadowTentaclesSpell() {
        super(
                "ShadowTentacles",
                "Summon shadow tentacles that grab and pull enemies",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.shadowtentacles.magic-cost", 70),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.shadowtentacles.cooldown", 8000),
                20.0,
                true,
                "Left Click"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        Player target = getTarget(player, range);
        if (target == null) return SpellResult.FAILURE;

        World world = player.getWorld();

        // Spawn shadow particles
        world.spawnParticle(Particle.SMOKE_LARGE, target.getLocation(), 30, 0.6,1,0.6);
        world.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1f, 0.6f);

        // Pull effect
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || target.isDead() || ticks > 20) {
                    cancel();
                    return;
                }

                Vector pull = player.getLocation().toVector()
                        .subtract(target.getLocation().toVector())
                        .normalize()
                        .multiply(0.7);

                target.setVelocity(pull);

                target.getWorld().spawnParticle(
                        Particle.SMOKE_NORMAL,
                        target.getLocation().add(0, 1, 0),
                        6, 0.3,0.4,0.3, 0.01
                );

                ticks++;
            }
        }.runTaskTimer(SpellCraftPlugin.getInstance(), 0, 2);

        // Damage when pulled
        target.damage(damage, player);

        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1f, 1.2f);

        return SpellResult.SUCCESS;
    }

    private Player getTarget(Player player, double range) {
        Player closest = null;
        double closestDist = range;

        for (Player p : player.getWorld().getPlayers()) {
            if (p.equals(player)) continue;

            double dist = p.getLocation().distance(player.getLocation());
            if (dist < closestDist) {
                closest = p;
                closestDist = dist;
            }
        }
        return closest;
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}
    @Override public boolean isSneakingAbility() { return false; }
    @Override public Action getAbilityActivationAction() { return Action.LEFT_CLICK_AIR; }
    @Override public MagicElement getElement() { return MagicElement.DARK; }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null
                ? currentLocation.clone()
                : new Location(Bukkit.getWorlds().getFirst(),0,0,0);
    }
}
