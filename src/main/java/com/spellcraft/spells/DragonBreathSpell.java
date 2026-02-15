package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
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
    private double radius = 5.0;
    private double damage = 6.0;

    public DragonBreathSpell() {
        super(
                "DragonBreath",
                "Unleash a fiery dragon breath around you",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.dragonbreath.magic-cost", 40),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.dragonbreath.cooldown", 8000),
                10.0, // range placeholder
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.dragonbreath.enabled", true),
                "Right Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        currentLocation = player.getLocation().clone();

        ThreadUtil.ensureLocationTimer(currentLocation, () -> {
            if (!player.isOnline() || player.isDead()) {
                remove();
                return;
            }

            // Spawn particles
            for (double angle = 0; angle < 360; angle += 20) {
                double radians = Math.toRadians(angle);
                double x = Math.cos(radians) * radius;
                double z = Math.sin(radians) * radius;
                Location particleLoc = currentLocation.clone().add(x, 1, z);
                if (particleLoc.getBlock().getType() == Material.AIR) {
                    particleLoc.getBlock().setType(Material.FIRE);
                }
                particleLoc.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0.05);
            }

            // Damage entities
            for (Entity entity : currentLocation.getWorld().getNearbyEntities(currentLocation, radius, 2, radius)) {
                if (entity instanceof LivingEntity living && !living.equals(player)) {
                    living.setFireTicks(80);
                    living.damage(damage, player);
                }
            }

        }, 0, 2);

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
    public @NotNull Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().getFirst(), 0,0,0);
    }
}
