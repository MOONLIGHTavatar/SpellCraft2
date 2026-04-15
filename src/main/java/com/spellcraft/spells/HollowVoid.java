package com.spellcraft.spells;

import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.DamageHandler;
import com.spellcraft.util.ParticleEffect;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class HollowVoid extends AbstractSpell {

    private Location currentLoc;
    private Location playerLoc;

    public HollowVoid() {
        super(
                "HollowVoid",
                "Light and darkness combine into a massive void orb",
                SpellCategory.COMBAT,
                40,
                9000L,
                35.0,
                true,
                "Sneak to fire Hollow Void"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {

        Player player = caster.getPlayer();

        double speed = 0.75;
        double hitRadius = 2.5; // ~5 block wide
        double damage = 12.0; // 6 hearts
        int maxLifetime = 80;

        currentLoc = player.getEyeLocation().clone();
        playerLoc = player.getLocation();

        Vector direction = player.getEyeLocation().getDirection().normalize();

        final int[] lived = {0};

        ThreadUtil.ensureLocationTimer(currentLoc, () -> {

            if (!player.isOnline() || player.isDead()) {
                remove();
                return;
            }

            if (lived[0]++ > maxLifetime) {
                explode(currentLoc);
                remove();
                return;
            }

            currentLoc.add(direction.clone().multiply(speed));

            if (currentLoc.getBlock().getType().isSolid()) {
                explode(currentLoc);
                remove();
                return;
            }

            // LIGHT
            ParticleEffect.END_ROD.display(currentLoc, 15);

            // DARK / BLACK
            ParticleEffect.SMOKE_LARGE.display(currentLoc, 12);

            // PURPLE VOID CORE
            ParticleEffect.DRAGON_BREATH.display(currentLoc, 25);
            ParticleEffect.PORTAL.display(currentLoc, 20);

            currentLoc.getWorld()
                    .getNearbyEntities(currentLoc, hitRadius, hitRadius, hitRadius)
                    .forEach(entity -> {

                        if (!(entity instanceof LivingEntity living)) return;
                        if (living.equals(player)) return;
                        if (!DamageHandler.isValidTarget(player, living)) return;

                        DamageHandler.damage(
                                player,
                                living,
                                damage,
                                getName(),
                                getElement()
                        );

                        explode(currentLoc);
                        remove();
                    });

        },0,1);

        return SpellResult.SUCCESS;
    }

    private void explode(Location loc) {

        ParticleEffect.DRAGON_BREATH.display(loc, 80);
        ParticleEffect.SMOKE_LARGE.display(loc, 40);
        ParticleEffect.END_ROD.display(loc, 30);

        loc.getWorld().playSound(
                loc,
                "entity.warden.sonic_boom",
                1f,
                0.7f
        );
    }

    @Override public void progress() {}

    @Override protected void onLoad() {}

    @Override protected void onStop() {}

    @Override public boolean isSneakingAbility() {
        return true;
    }

    @Override public Action getAbilityActivationAction() {
        return null;
    }

    @Override public MagicElement getElement() {
        return MagicElement.DARK;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLoc == null ? playerLoc : currentLoc;
    }
}