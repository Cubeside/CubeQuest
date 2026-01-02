package de.iani.cubequest.quests;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

import de.iani.cubequest.CubeQuest;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class ServerDependendQuest extends ProgressableQuest {

    private int serverId = -1;

    public ServerDependendQuest(int id, String name, Component displayMessage, int serverId) {
        super(id, name, displayMessage);

        this.serverId = serverId;
    }

    public ServerDependendQuest(int id, String name, Component displayMessage) {
        super(id, name, displayMessage);

        this.serverId = CubeQuest.getInstance().getServerId();
    }

    public ServerDependendQuest(int id) {
        super(id);

        this.serverId = CubeQuest.getInstance().getServerId();
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.serverId = yc.getInt("serverId");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {

        yc.set("serverId", this.serverId);

        return super.serializeToString(yc);
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        String serverName = getServerName();

        Component serverNameComp = (serverName == null) ? text("(Name unbekannt)", NamedTextColor.GOLD)
                : text(serverName, NamedTextColor.GREEN);

        Component thisServerComp = isForThisServer() ? text("(dieser Server)", NamedTextColor.GREEN)
                : text("(ein anderer Server)", NamedTextColor.GOLD);

        result.add(text("Server: ", NamedTextColor.DARK_AQUA).append(serverNameComp)
                .append(text(" [id: ", NamedTextColor.GREEN))
                .append(text(String.valueOf(this.serverId), NamedTextColor.GREEN))
                .append(text("] ", NamedTextColor.GREEN)).append(thisServerComp));

        result.add(empty());
        return result;
    }

    public boolean isForThisServer() {
        return CubeQuest.getInstance().getServerId() == this.serverId;
    }

    public int getServerId() {
        return this.serverId;
    }

    public String getServerName() {
        try {
            return (isForThisServer() ? CubeQuest.getInstance().getBungeeServerName()
                    : CubeQuest.getInstance().getDatabaseFassade().getServerName(this.serverId));
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not load server name for server with id " + this.serverId, e);
            return null;
        }
    }

    /**
     * Ruft KEIN QuestCreator#updateQuest auf! Sollte von aufrufender Quest getan werden!
     */
    protected void changeServerToThis() {
        this.serverId = CubeQuest.getInstance().getServerId();
    }

}
