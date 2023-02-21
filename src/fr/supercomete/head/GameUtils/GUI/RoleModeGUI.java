package fr.supercomete.head.GameUtils.GUI;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import fr.supercomete.head.role.KasterBorousCamp;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.supercomete.head.Exception.InvalidModeException;
import fr.supercomete.head.GameUtils.TeamManager;
import fr.supercomete.head.GameUtils.GameMode.ModeHandler.KtbsAPI;
import fr.supercomete.head.GameUtils.GameMode.ModeModifier.CampMode;
import fr.supercomete.head.GameUtils.GameMode.Modes.Mode;
import fr.supercomete.head.Inventory.InventoryUtils;
import fr.supercomete.head.core.Main;
import fr.supercomete.head.role.Role;

public class RoleModeGUI extends GUI {
    private static KtbsAPI api = Bukkit.getServicesManager().load(KtbsAPI.class);
	private static final CopyOnWriteArrayList<RoleModeGUI> allGui = new CopyOnWriteArrayList<RoleModeGUI>();
	private CampMode m;
	private Inventory inv;
	private int currentIndex = 0;
	private Player player;
    private CopyOnWriteArrayList<Class<?>> preformated;
    private ArrayList<KasterBorousCamp>primitives;
    public RoleModeGUI() {
			this.m = null;
			this.player = null;

	}
	public RoleModeGUI(Mode mode, Player player) {
		if (mode instanceof CampMode) {
			this.m = (CampMode) mode;

			this.player = player;
			if (player != null)
				allGui.add(this);
            CopyOnWriteArrayList<Class<?>> preformated = api.getModeProvider().getMode(m.getClass()).getRegisteredrole();
            ArrayList<KasterBorousCamp>primitives = new ArrayList<>();
            for(Class<?> claz : preformated){
                try{
                    Method method = claz.getMethod("getCamp",null);
                    Role role = (Role) claz.getConstructors()[0].newInstance(UUID.randomUUID());
                    KasterBorousCamp camp =(KasterBorousCamp) method.invoke(role);
                    if(!primitives.contains(camp)){
                        primitives.add(camp);
                    }
                }catch (NoSuchMethodException |InvocationTargetException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
            this.primitives=primitives;
            this.preformated=preformated;
            this.inv = generateinv(0);
		} else {
			try {
				throw new InvalidModeException("Error in " + this.getClass(), new Throwable());
			} catch (InvalidModeException e) {
				e.printStackTrace();
			}
		}
	}
	protected Inventory generateinv() {
		return generateinv(0);
	}
	protected Inventory generateinv(int index) {
		Inventory tmp = Bukkit.createInventory(null, 54,"§b"+((Mode)m).getName() + " Role");
		for (int e = 0; e < 9; e++) {
			tmp.setItem(e, InventoryUtils.createColorItem(Material.STAINED_GLASS_PANE, " ", 1,
                    TeamManager.getShortOfChatColor(primitives.get(index).getColor())));
		}
		for (int e = 0; e < 9; e++) {
			tmp.setItem(53 - e, InventoryUtils.createColorItem(Material.STAINED_GLASS_PANE, " ", 1,
					TeamManager.getShortOfChatColor(primitives.get(index).getColor())));
		}
		int i = 0;

		for (KasterBorousCamp camp : primitives) {
			tmp.setItem(i, InventoryUtils.createColorItem(Material.WOOL, "§r" + camp.getColor() + camp.getName(), 1,
					TeamManager.getShortOfChatColor(camp.getColor())));
			i++;
		}
//		 Main.getRoleTypeList(primitives.get(index));

		CopyOnWriteArrayList<Class<?>> formated = new CopyOnWriteArrayList<Class<?>>();
		
		for(Class<?> clz : preformated) {
			if(api.getRoleProvider().getRoleByClass(clz).getCamp().equals(primitives.get(index))){
				formated.add(clz);
			}
		}
		for (int e =0;e<formated.size();e++) {
			Class<?> r = formated.get(e);
			Role rt = api.getRoleProvider().getRoleByClass(r);
			List<String> strl = (rt.AskIfUnique())
					? Arrays.asList("§3Camp: " + rt.getDefaultCamp().getColor() + rt.getDefaultCamp().getName(),
							InventoryUtils.ClickBool)
					: Arrays.asList("§3Camp: " + rt.getDefaultCamp().getColor() + rt.getDefaultCamp().getName(),
                    InventoryUtils.ClickTypoAdd + "1", InventoryUtils.ClickTypoRemove + "1");
			final ItemStack it =InventoryUtils.createSkullItem(rt.AskHeadTag(),
					rt.getDefaultCamp().getColor() + rt.getName() + " " + ((rt.AskIfUnique())
							? (Main.currentGame.hasClassInRoleCompoMap(r) ? "§aOn" : "§cOff")
							: "§rx" + ((Main.currentGame.hasClassInRoleCompoMap(r))
									? Main.currentGame.getRoleCompoMap().get(r)
									: 0)),strl);
			it.setAmount( ((Main.currentGame.hasClassInRoleCompoMap(r))?Main.currentGame.getRoleCompoMap().get(r): 0));
			tmp.setItem(e + 9,it);
		}
		tmp.setItem(49, InventoryUtils.getItem(Material.ARROW, "§7Retour", Arrays.asList("§rRetour au règles")));
		return tmp;
	}

	public void open() {
		open(0);
	}

	public void open(int index) {
		this.currentIndex = index;
		this.inv = generateinv(index);
		player.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		for (RoleModeGUI role : allGui) {
			if (e.getInventory().equals(role.inv)) {
				int currentslot = e.getSlot();
				ClickType currentClick = e.getClick();
				e.setCancelled(true);
				switch (currentslot) {
				case 49:
					new ModeGUI((Mode)role.m, role.player).open();
					break;
				default:
					if (currentslot < role.primitives.size()) {
						role.open(currentslot);
					} else if (currentslot >= 9
							&& currentslot < api.getRoleProvider().getRolesByCamp(api.getModeProvider().getMode(role.m.getClass()),role.primitives.get(role.currentIndex)).size()
									+ 9) {
						
						Class<?> r = api.getRoleProvider().getRolesByCamp(api.getModeProvider().getMode(role.m.getClass()),role.primitives.get(role.currentIndex))
								.get(currentslot - 9);
						HashMap<Class<?>, Integer> array =Main.currentGame.getRoleCompoMap();
						Role rt =api.getRoleProvider().getRoleByClass(r);
						if (rt.AskIfUnique()) {
							if (array.containsKey(r)) {
								array.remove(r);
							} else {
								array.put(r, 1);
							}
						} else {
							if (currentClick.isRightClick()) {
								if (array.containsKey(r)) {
									array.put(r, array.get(r) + 1);
								} else {
									array.put(r, 1);
								}
							} else if (currentClick.isLeftClick()) {
								if (array.containsKey(r)) {
									if (array.get(r) > 1) {
										array.put(r, array.get(r) - 1);
									} else array.remove(r);
								}
							}
						}
						Main.currentGame.setRoleCompoMap(array);
						role.open(role.currentIndex);
						continue;
					}
				}
			}
		}
	}
	// Optimization --> Forget GUI that have been closed >|<
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		for (RoleModeGUI role : allGui) {
			if (e.getInventory().equals(role.inv)) {
				allGui.remove(role);
			}
		}
	}
}