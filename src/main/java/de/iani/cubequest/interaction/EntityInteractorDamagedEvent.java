package de.iani.cubequest.interaction;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EntityInteractorDamagedEvent<T extends Event & Cancellable>
        extends InteractorDamagedEvent<T> {
    
    private final Player player;
    
    public EntityInteractorDamagedEvent(T original, EntityInteractor interactor) {
        super(original, interactor);
        
        if (original instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) original).getDamager();
            this.player = getPlayerFromDamager(damager);
        } else if (original instanceof HangingBreakByEntityEvent) {
            Entity damager = ((HangingBreakByEntityEvent) original).getRemover();
            this.player = getPlayerFromDamager(damager);
        } else if (original instanceof EntityDeathEvent) {
            this.player = ((EntityDeathEvent) original).getEntity().getKiller();
        } else {
            this.player = null;
        }
    }
    
    private Player getPlayerFromDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            ProjectileSource shooter = ((Projectile) damager).getShooter();
            return (shooter instanceof Player) ? (Player) shooter : null;
        } else {
            return null;
        }
    }
    
    @Override
    public EntityInteractor getInteractor() {
        return (EntityInteractor) super.getInteractor();
    }
    
    @Override
    public Player getPlayer() {
        return this.player;
    }
    
    @Override
    public String getNoPermissionMessage() {
        return "Du kannst dieses Entity nicht verletzen!";
    }
    
}
