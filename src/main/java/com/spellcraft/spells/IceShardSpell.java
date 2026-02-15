package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class IceShardSpell extends AbstractSpell implements Listener {

    private Location currentLocation;
    private final Random random = new Random();
    private final double damageAmount = 6.0;
    private final double knockbackStrength = 0.5;

    public IceShardSpell() {
        super(
                "IceShard",
                "Shoots multiple ice shards from nearby ice or water",
                SpellCategory.OFFENSIVE,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.iceshard.magic-cost", 50),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.iceshard.cooldown", 7000),
                20.0,
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.iceshard.enabled", true),
                "Right Click while Sneaking"
        );

        Bukkit.getPluginManager().registerEvents(this, SpellCraftPlugin.getInstance());
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getEyeLocation().clone();

        for (int i = 0; i < 5; i++) {
            Snowball shard = player.launchProjectile(Snowball.class, getSpreadVector(player.getLocation().getDirection()));
            shard.setShooter(player);
            shard.setCustomName("IceShard");
        }

        player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 30, 1,1,1);
        player.playSound(player.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 1.2f);

        return SpellResult.SUCCESS;
    }

    private Vector getSpreadVector(Vector direction) {
        double spread = 0.25;
        double dx = direction.getX() + (random.nextDouble() - 0.5) * spread;
        double dy = direction.getY() + (random.nextDouble() - 0.5) * spread;
        double dz = direction.getZ() + (random.nextDouble() - 0.5) * spread;
        return new Vector(dx, dy, dz).normalize().multiply(1.6);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) return;
        if (!"IceShard".equals(event.getEntity().getCustomName())) return;

        if (event.getHitEntity() instanceof Player target) {
            target.damage(damageAmount);
            Vector knockback = target.getLocation().toVector().subtract(event.getEntity().getLocation().toVector())
                    .normalize().multiply(knockbackStrength);
            target.setVelocity(knockback);
            target.getWorld().spawnParticle(Particle.SNOWFLAKE, target.getLocation().add(0,1,0), 15,0.3,0.3,0.3);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f,1.2f);
        }
    }

    @Override
    public void progress() {}
    @Override
    protected void onLoad() {}
    @Override
    protected void onStop() {}
    @Override
    public boolean isSneakingAbility() { return true; }
    @Override
    public Action getAbilityActivationAction() { return Action.RIGHT_CLICK_AIR; }
    @Override
    public MagicElement getElement() { return MagicElement.ICE; }
    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(),0,0,0);
    }
}
