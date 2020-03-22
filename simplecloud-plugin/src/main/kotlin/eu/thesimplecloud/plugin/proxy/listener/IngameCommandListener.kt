package eu.thesimplecloud.plugin.proxy.listener

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.event.player.CloudPlayerCommandExecuteEvent
import eu.thesimplecloud.plugin.extension.getCloudPlayer
import eu.thesimplecloud.plugin.network.packets.PacketOutPlayerExecuteCommand
import eu.thesimplecloud.plugin.proxy.CloudProxyPlugin
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class IngameCommandListener : Listener {

    @EventHandler
    fun on(event: ChatEvent) {
        if (event.sender !is ProxiedPlayer) return
        val player = event.sender as ProxiedPlayer
        if (event.isCommand){
            val rawCommand = event.message.replaceFirst("/", "")
            val commandStart = rawCommand.split(" ")[0]
            if (CloudProxyPlugin.instance.synchronizedIngameCommandNamesContainer.names.contains(commandStart.toLowerCase())) {
                CloudPlugin.instance.communicationClient.sendUnitQuery(PacketOutPlayerExecuteCommand(player.getCloudPlayer(), rawCommand))
                event.isCancelled = true
            }
            CloudAPI.instance.getEventManager().call(CloudPlayerCommandExecuteEvent(player.uniqueId, player.name, rawCommand))
        }
    }

}