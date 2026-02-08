package com.spellcraft.spells;

import com.spellcraft.SpellCraftPlugin;
import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.BlockSource;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class AquaMissile extends AbstractSpell {

    private Location currentLoc;
    private Location playerLoc;

    public AquaMissile() {
        super(
                "AquaMissile",
                "Launch a homing water projectile at your enemy",
                SpellCategory.COMBAT,
                SpellCraftPlugin.getInstance().getConfig().getInt("spells.aqua-missile.magic-cost", 35),
                SpellCraftPlugin.getInstance().getConfig().getLong("spells.aqua-missile.cooldown", 6000L),
                SpellCraftPlugin.getInstance().getConfig().getDouble("spells.aqua-missile.range", 40.0),
                SpellCraftPlugin.getInstance().getConfig().getBoolean("spells.aqua-missile.enabled", true),
                "Sneak to launch a homing water orb!"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        var config = SpellCraftPlugin.getInstance().getConfig();

        double sourceRange = config.getDouble("spells.aqua-missile.source-range", 10.0);
        double speed = config.getDouble("spells.aqua-missile.speed", 0.6);
        double hitRadius = config.getDouble("spells.aqua-missile.hit-radius", 1.5);
        double damage = config.getDouble("spells.aqua-missile.damage", 6.0);
        int maxLifetime = config.getInt("spells.aqua-missile.max-ticks", 100);
        boolean requireSource = config.getBoolean("spells.aqua-missile.require-source", true);

        Block source = BlockSource.getSourceBlock(player, sourceRange);
        boolean hasWaterSource = source != null && source.getType() == Material.WATER;
        boolean hasPotion = player.getInventory().contains(Material.POTION);

        // Require source logic
        if (requireSource && !hasWaterSource && !hasPotion) {
            return SpellResult.FAILURE;
        }

        // Consume potion if no water source
        if (!hasWaterSource && hasPotion) {
            if (!consumeOnePotion(player)) {
                return SpellResult.FAILURE;
            }
        }

        // Use water block if present
        if (hasWaterSource) {
            ParticleEffect.WATER_SPLASH.display(source.getLocation().add(0.5, 0.5, 0.5), 25);
            source.setType(Material.AIR);
            currentLoc = source.getLocation().add(0.5, 0.5, 0.5);
        } else {
            currentLoc = player.getEyeLocation().clone();
        }

        playerLoc = player.getLocation();

        final int[] lived = {0};

        ThreadUtil.ensureLocationTimer(currentLoc, () -> {

            if (!player.isOnline() || player.isDead()) {
                remove();
                return;
            }

            if (lived[0]++ > maxLifetime) {
                splash(currentLoc);
                remove();
                return;
            }

            LivingEntity target = findNearestTarget(player, getRange());
            Vector direction;

            if (target != null) {
                direction = target.getEyeLocation().toVector()
                        .subtract(currentLoc.toVector())
                        .normalize();
            } else {
                direction = player.getEyeLocation().getDirection().normalize();
            }

            currentLoc.add(direction.multiply(speed));
            playerLoc = player.getLocation();

            if (currentLoc.getBlock().getType().isSolid()) {
                splash(currentLoc);
                remove();
                return;
            }

            // Water visuals
            ParticleEffect.WATER_SPLASH.display(currentLoc, 6);
            ParticleEffect.WATER_BUBBLE.display(currentLoc, 3);

            currentLoc.getWorld().getNearbyEntities(currentLoc, hitRadius, hitRadius, hitRadius)
                    .forEach(entity -> {
                        if (!(entity instanceof LivingEntity living)) return;
                        if (living.equals(player)) return;
                        if (!DamageHandler.isValidTarget(player, living)) return;

                        DamageHandler.damage(player, living, damage, getName(), getElement());
                        splash(currentLoc);
                        remove();
                    });

        }, 0, 1);

        return SpellResult.SUCCESS;
    }

    private boolean consumeOnePotion(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != Material.POTION) continue;

            // Ensure it is specifically a WATER potion
            if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.PotionMeta meta)) continue;
            if (meta.getBasePotionData().getType() != org.bukkit.potion.PotionType.WATER) continue;

            // Remove one water bottle
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().remove(item);
            }

            // Give back an empty glass bottle
            player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE, 1));
            return true;
        }
        return false;
    }

    private LivingEntity findNearestTarget(Player player, double range) {
        return player.getNearbyEntities(range, range, range).stream()
                .filter(e -> e instanceof LivingEntity living && !living.equals(player))
                .map(e -> (LivingEntity) e)
                .filter(e -> DamageHandler.isValidTarget(player, e))
                .min((a, b) -> Double.compare(
                        a.getLocation().distanceSquared(player.getLocation()),
                        b.getLocation().distanceSquared(player.getLocation())
                ))
                .orElse(null);
    }

    private void splash(Location loc) {
        ParticleEffect.WATER_SPLASH.display(loc, 20);
        ParticleEffect.CLOUD.display(loc, 10);
        loc.getWorld().playSound(loc, "entity.generic.splash", 1f, 1f);
    }

    @Override public void progress() {}
    @Override protected void onLoad() {}
    @Override protected void onStop() {}

    @Override public boolean isSneakingAbility() { return true; }
    @Override public Action getAbilityActivationAction() { return null; }
    @Override public MagicElement getElement() { return MagicElement.WATER; }

    @Override
    public @NotNull Location getLocation() {
        return currentLoc == null ? playerLoc : currentLoc;
    }
}
