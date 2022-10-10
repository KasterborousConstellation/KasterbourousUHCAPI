package fr.supercomete.commands;

import fr.supercomete.head.GameUtils.GameMode.ModeHandler.MapHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.supercomete.ServerExchangeProtocol.File.PlayerAccountManager;
import fr.supercomete.ServerExchangeProtocol.Rank.Rank;
import fr.supercomete.datamanager.FileManager.Fileutils;
import fr.supercomete.head.core.Main;
import fr.supercomete.head.world.worldgenerator;

import java.io.File;

public class BlackSeedCommand implements CommandExecutor {
	@SuppressWarnings("unused")
	private final Main main;
	public BlackSeedCommand(Main main) {
		this.main=main;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] arg3) {
		if(sender instanceof Player){
            Player player=(Player)sender;
            if(cmd.getName().equalsIgnoreCase("blacklistseed") ||cmd.getName().equalsIgnoreCase("blseed")) {
				if(PlayerAccountManager.getPlayerAccount(player).hasRank(Rank.Admin)) {
                    assert MapHandler.getMap() != null;
                    if(player.getWorld().equals(MapHandler.getMap().getPlayWorld())) {
						final File file = new File("/var/games/minecraft/Library/Seeds/",""+Main.generator.getBiome());
						final String content = Fileutils.loadContent(file);
						final String[] mod = content.split(MapHandler.getMap().getPlayWorld().getSeed()+"L,");
						StringBuilder end = new StringBuilder();
						for(String s : mod) end.append(s);
						Fileutils.save(file, end.toString());
						player.sendMessage(Main.UHCTypo+"La seed "+MapHandler.getMap().getPlayWorld().getSeed()+" est maintenant blacklist.");
					}else player.sendMessage(Main.UHCTypo+"Vous êtes pas dans le bon monde");
				}else {
					player.sendMessage(Main.UHCTypo+"§cErreur vous n'avez pas le droit d'utiliser cette commande.");
					return false;
				}
				return true;
				
			}
		}
		return false;
	}
}