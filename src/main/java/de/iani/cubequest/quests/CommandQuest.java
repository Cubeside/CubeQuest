package de.iani.cubequest.quests;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;

public class CommandQuest extends Quest {

    private HashSet<String> commands;
    private HashSet<String[]> args;
    private boolean caseSensitive;

    /**
     * Erzeugt eine CommandQuest, bei der der Spieler einen bestimmten Befehl eingeben muss.
     * Der Befehl kann durch genaue Argumente spezifiziert werden.
     * @param name Name der Quest
     * @param giveMessage Nachricht, die der Spieler beim Start der Quest erhählt.
     * @param successMessage Nachricht, die der Spieler bei Abschluss der Quest erhählt.
     * @param successReward Belohnung, die der Spieler bei Abschluss der Quest erhählt.
     * @param commands Collection der Befehle, die der Spieler eingeben kann, um die Quest zu erfüllen.
     * @param args Collection von Argumenten, die der Spieler eingeben kann, um die Quest zu erfüllen. Null-Argumente sind immer erfüllt.
     * @param caseSensitive ob die Argumente case-senstitive sind (commands sind nie case-sensitive).
     */
    public CommandQuest(String name, String giveMessage, String successMessage, Reward successReward,
            Collection<String> commands, Collection<String[]> args, boolean caseSensitive) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(commands);
        Verify.verify(!commands.isEmpty());
        Verify.verifyNotNull(args);

        this.caseSensitive = caseSensitive;
        this.commands = new HashSet<String>();
        for (String s: commands) {
            commands.add(s.toLowerCase());
        }
        this.args = new HashSet<String[]>();
        for (String[] s: args) {
            this.args.add(Arrays.copyOf(s, s.length));
        }
    }

    @Override
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        if (getPlayerStatus(event.getPlayer().getUniqueId()) != Status.GIVENTO) {
            return;
        }
        String[] parts = event.getMessage().split(" ");
        if (parts.length < 1) {
            return;
        }
        String cmd = parts[0].substring(1).toLowerCase();
        if (!commands.contains(cmd)) {
            return;
        }
        String[] args = new String[parts.length - 1];
        for (int i=1; i<parts.length; i++) {
            args[i-1] = parts[i];
        }
        outerloop:
        for (String[] s: this.args) {
            for (int i=0; i<s.length; i++) {
                if (i >= args.length) {
                    continue outerloop;
                }
                if (caseSensitive) {
                    if (s[i] != null && !args[i].equals(s[i])) {
                        continue outerloop;
                    }
                } else {
                    if (s[i] != null && !args[i].equalsIgnoreCase(s[i])) {
                        continue outerloop;
                    }
                }
            }
            // onSuccess wird erst im nächsten Tick ausgelöst, damit der Befehl vorher durchlaufen kann
            Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
                onSuccess(event.getPlayer());
            }, 1L);
        }
    }

}
