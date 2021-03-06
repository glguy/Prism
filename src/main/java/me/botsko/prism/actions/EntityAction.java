package me.botsko.prism.actions;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.appliers.ChangeResult;
import me.botsko.prism.appliers.ChangeResultType;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Villager.Profession;

public class EntityAction extends GenericAction {
	
	public class EntityActionData {
		public String entity_name;
		public String custom_name;
		public boolean isAdult;
		public boolean sitting;
		public String color;
		public String newColor;
		public String profession;
		public String taming_owner;
	}
	
	/**
	 * 
	 */
	protected EntityActionData actionData;
	

	/**
	 * 
	 * @param action_type
	 * @param block
	 * @param player
	 */
	public void setEntity( Entity entity, String dyeUsed ){
		
		// Build an object for the specific details of this action
		actionData = new EntityActionData();
				
		if( entity != null && entity.getType() != null && entity.getType().name() != null ){
			this.actionData.entity_name = entity.getType().name().toLowerCase();
			this.world_name = entity.getWorld().getName();
			this.x = entity.getLocation().getBlockX();
			this.y = entity.getLocation().getBlockY();
			this.z = entity.getLocation().getBlockZ();
			
			
			// Get custom name
			if( entity instanceof LivingEntity ){
				this.actionData.custom_name = ((LivingEntity)entity).getCustomName();
			}
			
			// Get animal age
			if(entity instanceof Ageable && !(entity instanceof Monster) ){
				Ageable a = (Ageable)entity;
				this.actionData.isAdult = a.isAdult();
			} else {
				this.actionData.isAdult = true;
			}
			
			// Get current sheep color
			if( entity.getType().equals(EntityType.SHEEP)){
				Sheep sheep = ((Sheep) entity);
				this.actionData.color = sheep.getColor().name().toLowerCase();
			}
			
			// Get color it will become
			if(dyeUsed != null){
				this.actionData.newColor = dyeUsed;
			}
			
			// Get villager type
			if( entity instanceof Villager ){
				Villager v = (Villager)entity;
				this.actionData.profession = v.getProfession().toString().toLowerCase();
			}
			
			// Wolf details
			if (entity instanceof Wolf){
	            Wolf wolf = (Wolf)entity;
	            
	            // Owner
	            if(wolf.isTamed()){
	                if(wolf.getOwner() instanceof Player){
	                	this.actionData.taming_owner = ((Player)wolf.getOwner()).getName();
	                }
	                if(wolf.getOwner() instanceof OfflinePlayer){
	                	this.actionData.taming_owner = ((OfflinePlayer)wolf.getOwner()).getName();
	                }
	            }
	            
	            // Collar color
	            this.actionData.color = wolf.getCollarColor().name().toLowerCase();
	            
	            // Sitting
	            if( wolf.isSitting() ){
	            	this.actionData.sitting = true;
	            }
	            
	    	}
		}
	}
	
	
	/**
	 * 
	 */
	public void save(){
		data = gson.toJson(actionData);
	}
	
	
	/**
	 * 
	 */
	public void setData( String data ){
		if(data != null){
			actionData = gson.fromJson(data, EntityActionData.class);
		}
	}
	
	
	/**
	 * 
	 * @return
	 */
	public EntityType getEntityType(){
		try {
			EntityType e = EntityType.valueOf(actionData.entity_name.toUpperCase());
			if(e != null){
				return e;
			}
		} catch(IllegalArgumentException e){
			// In pre-RC builds we logged the wrong name of entities, sometimes the names
			// don't match the enum. 
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isAdult(){
		return this.actionData.isAdult;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isSitting(){
		return this.actionData.sitting;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public DyeColor getColor(){
		if(actionData.color != null){
			return DyeColor.valueOf(actionData.color.toUpperCase());
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Profession getProfession(){
		if(actionData.profession != null){
			return Profession.valueOf(actionData.profession.toUpperCase());
		}
		return null;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getTamingOwner(){
		return this.actionData.taming_owner;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getCustomName(){
		return this.actionData.custom_name;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getNiceName(){
		String name = "";
		if(actionData.color != null && !actionData.color.isEmpty()){
			name += actionData.color + " ";
		}
		if(actionData.isAdult && !actionData.isAdult){
			name += "baby ";
		}
		if(this.actionData.profession != null){
			name += this.actionData.profession + " ";
		}
		if(actionData.taming_owner != null){
			name += actionData.taming_owner+"'s ";
		}
		name += actionData.entity_name;
		if(this.actionData.newColor != null){
			name += " " + this.actionData.newColor;
		}
		if(this.actionData.custom_name != null){
			name += " named " + this.actionData.custom_name;
		}
		return name;
	}
	
	
	/**
	 * 
	 */
	public ChangeResult applyRollback( Player player, QueryParameters parameters, boolean is_preview ){
		
		if(getEntityType() == null){
			return new ChangeResult( ChangeResultType.SKIPPED, null );
		}
		
		if( Prism.getIllegalEntities().contains( getEntityType().name().toLowerCase() ) ){
			return new ChangeResult( ChangeResultType.SKIPPED, null );
		}
		
		if( !is_preview ){
		
			Location loc = getLoc();
			
			loc.setX( loc.getX()+0.5 );
			loc.setZ( loc.getZ()+0.5 );
			
			Entity entity = loc.getWorld().spawnEntity(loc, getEntityType());
			
			// Get custom name
			if( entity instanceof LivingEntity && getCustomName() != null ){
				LivingEntity namedEntity = (LivingEntity)entity;
				namedEntity.setCustomName( getCustomName() );
			}
			
			// Get animal age
			if(entity instanceof Ageable){
				Ageable age = (Ageable)entity;
				if(!isAdult()){
					age.setBaby();
				}
			}
			
			// Set sheep color
			if( entity.getType().equals(EntityType.SHEEP) && getColor() != null ){
				Sheep sheep = ((Sheep) entity);
				sheep.setColor( getColor() );
			}
			
			// Set villager profession
			if( entity instanceof Villager && getProfession() != null ){
				Villager v = (Villager)entity;
				v.setProfession( getProfession() );
			}
			
			// Set wolf details
			if (entity instanceof Wolf){
				
				// Owner
	            Wolf wolf = (Wolf)entity;
	            String tamingOwner = getTamingOwner();
	            if(tamingOwner != null){
		            Player owner = plugin.getServer().getPlayer( tamingOwner );
		            if(owner == null){
			            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer( tamingOwner );
			            if(offlinePlayer.hasPlayedBefore()){
			            	owner = offlinePlayer.getPlayer();
			            }
		            }
		            if(owner != null) wolf.setOwner(owner);
	            }
	            
	            // Collar color
	            if( getColor() != null ){
	            	wolf.setCollarColor( getColor() );
	            }
	            
	            if(isSitting()){
	            	wolf.setSitting(true);
	            }
	    	}
			return new ChangeResult( ChangeResultType.APPLIED, null );
		}
		return new ChangeResult( ChangeResultType.PLANNED, null );
	}
}