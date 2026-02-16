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
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MagmaBarrageSpell extends AbstractSpell implements Listener {

    private Location currentLocation;
    private final Random random = new Random();
    private final double damageAmount = 5.0;

    public MagmaBarrageSpell() {
        super(
                "MagmaBarrage",
                "Unleash a rapid barrage of magma shots",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.magmabarrage.magic-cost", 60),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.magmabarrage.cooldown", 6000),
                20.0,
                true,
                "Left Click"
        );

        Bukkit.getPluginManager().registerEvents(this, SpellCraftPlugin.getInstance());
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getEyeLocation().clone();

        // Shoot many magma shots
        for (int i = 0; i < 8; i++) {
            SmallFireball fireball = player.launchProjectile(
                    SmallFireball.class,
                    getSpreadVector(player.getLocation().getDirection())
            );

            fireball.setShooter(player);
            fireball.setIsIncendiary(true);
            fireball.setYield(0);
            fireball.setCustomName("MagmaShot");
        }

        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation(), 40, 1,1,1);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 0.9f);

        return SpellResult.SUCCESS;
    }

    private Vector getSpreadVector(Vector direction) {
        double spread = 0.35;
        double dx = direction.getX() + (random.nextDouble() - 0.5) * spread;
        double dy = direction.getY() + (random.nextDouble() - 0.5) * spread;
        double dz = direction.getZ() + (random.nextDouble() - 0.5) * spread;
        return new Vector(dx, dy, dz).normalize().multiply(1.4);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof SmallFireball)) return;
        if (!"MagmaShot".equals(event.getEntity().getCustomName())) return;

        if (event.getHitEntity() instanceof Player target) {
            target.damage(damageAmount);
            target.setFireTicks(60); // burn 3 seconds

            target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0,1,0), 20,0.4,0.4,0.4);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f,1f);
        }
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
