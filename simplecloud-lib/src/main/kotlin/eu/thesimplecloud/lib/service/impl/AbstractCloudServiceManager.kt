package eu.thesimplecloud.lib.service.impl

import eu.thesimplecloud.lib.CloudLib
import eu.thesimplecloud.lib.events.service.*
import eu.thesimplecloud.lib.service.ICloudService
import eu.thesimplecloud.lib.service.ICloudServiceManager
import eu.thesimplecloud.lib.service.ServiceState
import java.util.concurrent.CopyOnWriteArrayList

abstract class AbstractCloudServiceManager : ICloudServiceManager {

    private val services = CopyOnWriteArrayList<ICloudService>()

    override fun updateCloudService(cloudService: ICloudService) {
        val cashedService = getCloudService(cloudService.getName())
        if (cashedService == null){
            this.services.add(cloudService)
            CloudLib.instance.getEventManager().call(CloudServiceRegisteredEvent(cloudService))
            return
        }
        val nowStarting = cashedService.getState() == ServiceState.PREPARED && cloudService.getState() == ServiceState.STARTING
        val nowJoinable = !cashedService.isJoinable() && cloudService.isJoinable()
        val nowConnected = !cashedService.isAuthenticated() && cloudService.isAuthenticated()

        cashedService.setMOTD(cloudService.getMOTD())
        cashedService.setOnlinePlayers(cloudService.getOnlinePlayers())
        cashedService.setState(cloudService.getState())
        cashedService.setAuthenticated(cloudService.isAuthenticated())
        cashedService.setLastUpdate(System.currentTimeMillis())
        cashedService as DefaultCloudService
        cashedService.setWrapperName(cloudService.getWrapperName())

        CloudLib.instance.getEventManager().call(CloudServiceUpdatedEvent(cashedService))

        if (nowStarting) {
            CloudLib.instance.getEventManager().call(CloudServiceStartingEvent(cashedService))
            cashedService.startingPromise().trySuccess(Unit)
        }
        if (nowConnected) {
            CloudLib.instance.getEventManager().call(CloudServiceConnectedEvent(cashedService))
            cashedService.connectedPromise().trySuccess(Unit)
        }
        if (nowJoinable) {
            CloudLib.instance.getEventManager().call(CloudServiceJoinableEvent(cashedService))
            cashedService.joinablePromise().trySuccess(Unit)
        }
    }

    override fun removeCloudService(name: String) {
        getCloudService(name)?.let {
            this.services.remove(it)
            CloudLib.instance.getEventManager().call(CloudServiceUnregisteredEvent(it))
            it.closedPromise().trySuccess(Unit)
        }
    }

    override fun getAllCloudServices(): List<ICloudService> = this.services
}