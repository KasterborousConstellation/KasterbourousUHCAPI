package fr.supercomete.tasks;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import fr.supercomete.head.GameUtils.Events.GameEvents.Event;
import fr.supercomete.head.GameUtils.Game;
import fr.supercomete.head.GameUtils.GameConfigurable.Configurable;
import fr.supercomete.head.GameUtils.GameMode.ModeHandler.MapHandler;
import fr.supercomete.head.GameUtils.GameMode.Modes.Mode;
import fr.supercomete.head.GameUtils.Time.TimeUtility;
import fr.supercomete.head.role.Triggers.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.supercomete.enums.GenerationMode;
import fr.supercomete.enums.Gstate;
import fr.supercomete.head.GameUtils.Scenarios.Scenarios;
import fr.supercomete.head.GameUtils.GameMode.ModeModifier.CampMode;
import fr.supercomete.head.GameUtils.Time.Timer;
import fr.supercomete.head.GameUtils.Time.TimerType;
import fr.supercomete.head.PlayerUtils.Offline_Player;
import fr.supercomete.head.core.Main;
import fr.supercomete.head.role.Role;
import fr.supercomete.head.role.RoleHandler;
import fr.supercomete.head.world.worldgenerator;
public class Cycle extends BukkitRunnable{
	private final Main main;
	public Cycle(Main main) {
		this.main=main;
		hasPvpForced= false;
		hasBordureForced=false;
		hasForceRole=false;
		Main.currentCycle=this;
	}
	int days=0;int time=0;int timer;
	public static boolean hasPvpForced=false;
	public static boolean hasBordureForced=false;
	public static boolean hasForceRole=false;
    public Mode mode = Main.currentGame.getMode();
    public Game game = Main.currentGame;
	//AnnoncedTimes 
	private final int[] annonced=new int[] {10*60,5*60,3*60,60,30,10,5,4,3,2,1};
	@Override
	public void run() {
        if(Main.getPlayerlist().size()>0){
            if (time == 0) {
                timer = ((game.getTimer(Timer.EpisodeTime)).getData() / 2) - 1;
                ArrayList<Player> pllist = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers())
                    if (player.getGameMode() != GameMode.SPECTATOR) pllist.add(player);
                for (Player pl : pllist) {
                    mode.OnStart(pl);
                }
            }
            if ((time == game.getTimer(Timer.BorderTime).getData() || main.isForcebordure()) && !hasBordureForced) {
                hasBordureForced = true;
                Bukkit.broadcastMessage(Main.UHCTypo + "La bordure est en mouvement");
            }
            if (!game.isGameState(Gstate.Waiting) && !game.isGameState(Gstate.Starting) && time > 0) {
                game.setEpisode(days);
                game.setTime(time);
            }
            /*
            Game event implementation
             */
            for(final Event event : game.getGameEvents()){
                if(time == event.getExecutionTime()){
                    event.onExecutionTime();
                }
            }
            /*
            While Any, While Night, While Day
                implementation
            */
            mode.onGlobalAnytime(time);
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!Main.getPlayerlist().contains(player.getUniqueId())) {
                    player.setGameMode(GameMode.SPECTATOR);
                } else {
                    if (game.getTime() >= 0) {
                        mode.onAnyTime(player);
                        if (RoleHandler.getRoleOf(player) != null) {
                            Role role = RoleHandler.getRoleOf(player);
                            if (role instanceof Trigger_WhileAnyTime) {
                                ((Trigger_WhileAnyTime) role).WhileAnyTime(player);
                            }
                            if (role instanceof Trigger_WhileDay) {
                                if (game.getGamestate().equals(Gstate.Day)) {
                                    ((Trigger_WhileDay) role).WhileDay(player);
                                }
                            }
                            if (role instanceof Trigger_WhileNight) {
                                if (game.getGamestate().equals(Gstate.Night)) {
                                    ((Trigger_WhileNight) role).WhileNight(player);
                                }
                            }
                        }
                    }
                }
            }
            /*
            Episode implementation
             */
            if (time % game.getTimer(Timer.RealEpisodeTime).getData() == 0) {
                days++;
                for (UUID uu : Main.getPlayerlist()) {
                    Player player = Bukkit.getPlayer(uu);
                    mode.onEpisodeTime(player);
                }
                for(Role role : RoleHandler.getRoleList().values()){
                    if(role instanceof Trigger_onEpisodeTime){
                        Trigger_onEpisodeTime episodeTime = (Trigger_onEpisodeTime)role;
                        episodeTime.onEpisodeTime(Bukkit.getPlayer(role.getOwner()));
                    }
                }
                Bukkit.broadcastMessage("§eEpisode §r" + days + "");
            }
            /*
            Night switch
             */
            if (timer == (game.getTimer(Timer.EpisodeTime)).getData() / 2) {
                game.setGamestate(Gstate.Day);
                MapHandler.getMap().getPlayWorld().setTime(1000);
                Bukkit.broadcastMessage(Main.UHCTypo + "§6Le jour se §elève§6.");
                for (UUID uu : Main.getPlayerlist()) {
                    Player player = Bukkit.getPlayer(uu);
                    mode.onDayTime(player);
                    if (!(RoleHandler.getRoleOf(player) == null)) {
                        Role role = RoleHandler.getRoleOf(player);
                        if (role instanceof Trigger_OnDayTime) {
                            ((Trigger_OnDayTime) role).onDayTime(player);
                        }
                    }

                }
            }
            /*
            PvP time implementation
             */
            if ((time == game.getTimer(Timer.PvPTime).getData() || main.isForcedpvp()) && !hasPvpForced) {
                hasPvpForced = true;
                MapHandler.getMap().getPlayWorld().setPVP(true);
                Bukkit.broadcastMessage(Main.UHCTypo + "§6Le PVP est activé");
                if (game.getScenarios().contains(Scenarios.FinalHeal)) Main.finalheal();
            }
            /*
            Day switch
             */
            if (timer == (game.getTimer(Timer.EpisodeTime)).getData()) {
                game.setGamestate(Gstate.Night);
                MapHandler.getMap().getPlayWorld().setTime(18000);
                Bukkit.broadcastMessage(Main.UHCTypo + " §9La nuit §btombe§9.");
                timer = 0;
                for (UUID uu : Main.getPlayerlist()) {
                    Player player = Bukkit.getPlayer(uu);
                    mode.onNightTime(player);
                    if (!(RoleHandler.getRoleOf(player) == null)) {
                        Role role = RoleHandler.getRoleOf(player);
                        if (role instanceof Trigger_onNightTime) {
                            ((Trigger_onNightTime) role).onNightTime(player);
                        }
                    }
                }
            }
            /*
            Test win condition && update RoleState (used for temporary RoleState)
             */
            if (time % 5 == 0 && time > 20) {
                if (!(Main.devmode)) {
                    if (mode.WinCondition()) {
                        main.StopGame(null);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            mode.onEndingTime(player);
                        }
                        cancel();
                    }
                }
                if (RoleHandler.IsRoleGenerated()) {
                    for (Role role : RoleHandler.getRoleList().values()) {
                        role.updateRoleState();
                    }
                }
            }
            /*
            Implementation of Role Time && forcing role
             */
            if (mode instanceof CampMode && (time == game.getTimer(Timer.RoleTime).getData() || main.isForceRole()) && !hasForceRole) {
                hasForceRole = true;
                if (mode instanceof CampMode) {
                    RoleHandler.GiveRole();
                }
                for (Player player : Bukkit.getOnlinePlayers()) {
                    mode.onRoleTime(player);
                    if (!(RoleHandler.getRoleOf(player) == null)) {
                        Role role = RoleHandler.getRoleOf(player);
                        if (role instanceof Trigger_OnRoletime) {
                            ((Trigger_OnRoletime) role).onRoleTime(player);
                        }
                    }

                }
            }
            /*
            Announcing Timer if they are TimeDependent
             */
            for (int ann : this.annonced) {
                String str = TimeUtility.transform(ann, "§b", "§b", "§b");
                for (Timer timer : Timer.values()) {
                    if (game.getTimer(timer) != null) {
                        if (timer.getType() == TimerType.TimeDependent && timer.isDraw() && game.getTimer(timer).getData() - time == ann) {
                            Bukkit.broadcastMessage(Main.UHCTypo + timer.getName() + " dans " + str);
                        }
                    }

                }
            }
            /*
            Check for every player if they are disconnected for more than Disconnect Time
             */
            if (time % 20 == 0) {
                for (Offline_Player offplayer : game.getOfflinelist()) {
                    ArrayList<UUID> uuid = new ArrayList<UUID>();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        uuid.add(player.getUniqueId());
                    }
                    if (!uuid.contains(offplayer.getPlayer())) {
                        if (offplayer.getTimeElapsedSinceDeconnexion() > game.getDataFrom(Configurable.LIST.DecTime)) {
                            if (Main.getPlayerlist().contains(offplayer.getPlayer())) {
                                mode.DecoKillMethod(offplayer);
                                mode.ModeDefaultOnDeath(offplayer,offplayer.getLocation());
                            }
                        }
                    }
                }
            }

            timer++;
            time++;
        }
        if (main.getGenmode() == GenerationMode.None || game.isGameState(Gstate.Waiting)||Main.currentCycle!=this) {
            game.setGamestate(Gstate.Waiting);
            for (Player player : Bukkit.getOnlinePlayers()) {
                mode.onEndingTime(player);
            }
            cancel();
        }
	}
}