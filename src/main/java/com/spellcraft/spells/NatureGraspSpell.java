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

public class NatureGraspSpell extends AbstractSpell {

    private Location currentLocation;
    private final double damagePerTick = 1.5;

    public NatureGraspSpell() {
        super(
                "NatureGrasp",
                "Vines erupt and trap enemies in place",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.naturegrasp.magic-cost", 65),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.naturegrasp.cooldown", 9000),
                18.0,
                true,
                "Left Click"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();
        World world = player.getWorld();

        Location center = player.getLocation().add(player.getLocation().getDirection().multiply(6));

        // Nature burst
        world.spawnParticle(Particle.VILLAGER_HAPPY, center, 40, 1,1,1, 0.1);
        world.spawnParticle(Particle.BLOCK_CRACK, center, 40, 1,0.5,1, 
                Bukkit.createBlockData("oak_leaves"));
        world.playSound(center, Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);

        for (Entity entity : world.getNearbyEntities(center, 4, 3, 4)) {
            if (entity instanceof Player target && !target.equals(player)) {
                trapTarget(player, target);
            }
        }

        return SpellResult.SUCCESS;
    }

    private void trapTarget(Player caster, Player target) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || target.isDead() || ticks > 40) {
                    cancel();
                    return;
                }

                // Root them
                target.setVelocity(new Vector(0, -0.1, 0));

                // Vine particles
                target.getWorld().spawnParticle(
                        Particle.BLOCK_CRACK,
                        target.getLocation().add(0, 0.2, 0),
                        8, 0.4,0.1,0.4,
                        Bukkit.createBlockData("moss_block")
                );

                // Damage over time
                if (ticks % 10 == 0) {
                    target.damage(damagePerTick, caster);
                    target.playSound(target.getLocation(), Sound.BLOCK_VINE_BREAK, 1f, 1f);
                }

                ticks++;
            }
        }.runTaskTimer(SpellCraftPlugin.getInstance(), 0, 1);
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}
    @Override public boolean isSneakingAbility() { return false; }
    @Override public Action getAbilityActivationAction() { return Action.LEFT_CLICK_AIR; }
    @Override public MagicElement getElement() { return MagicElement.NATURE; }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null
                ? currentLocation.clone()
                : new Location(Bukkit.getWorlds().getFirst(),0,0,0);
    }
}
