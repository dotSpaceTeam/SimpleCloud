package eu.thesimplecloud.plugin.impl

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.clientserverapi.lib.packet.packetsender.sendQuery
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise
import eu.thesimplecloud.api.network.packets.service.PacketIOStartCloudService
import eu.thesimplecloud.api.network.packets.servicegroup.PacketIOCreateServiceGroup
import eu.thesimplecloud.api.network.packets.servicegroup.PacketIODeleteServiceGroup
import eu.thesimplecloud.api.network.packets.servicegroup.PacketIOUpdateCloudServiceGroup
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.servicegroup.ICloudServiceGroup
import eu.thesimplecloud.api.servicegroup.impl.AbstractCloudServiceGroupManager
import eu.thesimplecloud.plugin.startup.CloudPlugin

class CloudServiceGroupManagerImpl : AbstractCloudServiceGroupManager() {

    override fun updateGroup(cloudServiceGroup: ICloudServiceGroup, fromPacket: Boolean) {
        super.updateGroup(cloudServiceGroup, fromPacket)
        if (!fromPacket) CloudPlugin.instance.communicationClient.sendUnitQuery(PacketIOUpdateCloudServiceGroup(cloudServiceGroup))
    }

    override fun createServiceGroup(cloudServiceGroup: ICloudServiceGroup): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.communicationClient.sendUnitQuery(PacketIOCreateServiceGroup(cloudServiceGroup))
    }

    override fun startNewService(cloudServiceGroup: ICloudServiceGroup): ICommunicationPromise<ICloudService> {
        val namePromise = CloudPlugin.instance.communicationClient.sendQuery<String>(PacketIOStartCloudService(cloudServiceGroup.getName()))
        return namePromise.then { CloudAPI.instance.getCloudServiceManger().getCloudServiceByName(it)!! }
    }

    override fun deleteServiceGroup(cloudServiceGroup: ICloudServiceGroup): ICommunicationPromise<Unit> {
        return CloudPlugin.instance.communicationClient.sendQuery(PacketIODeleteServiceGroup(cloudServiceGroup.getName()))
    }

}