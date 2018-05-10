package tdn.api

import com.tdnsecuredrest.User
import grails.converters.JSON
import org.grails.web.json.JSONArray

class NotificationController {

    transient springSecurityService
    static transients = ['springSecurityService']

    def index() {
        List<Notification> notificationList = Notification.findAllByTo(User.get(springSecurityService.principal.id)).sort{a,b->b.date<=>a.date}
        JSONArray arr = new JSONArray()
        notificationList.forEach {
            p -> def json = JSON.parse((p as JSON).toString())
                json.put("fromName", p.from.name)
                arr.put(json)
        }
        render arr as JSON
           //.findAll("from Notification as n where n.destUser = ? order by n.date desc", [User.get(springSecurityService.principal.id)]) as JSON
    }

    def count() {
        def count = [notificationCount: Notification.countByToAndRead(User.get(springSecurityService.principal.id), false)]
        render count as JSON
    }

    def read() {
        User au = User.get(springSecurityService.principal.id)
        //Notification.executeUpdate("update Notification n set n.read = true where n.destUser = :user", [user: User.get(springSecurityService.principal.id)])
        List<Node> nodes = Notification.executeQuery("match (u:User {username:${au.username}})<-[n:NOTIFICATION]-() set n.read = TRUE return u")
        au.withTransaction {au.save()}
        render(status:200, [] as JSON)
    }

    def delete(Long id) {
        Notification.get(id).withTransaction {Notification.get(id).delete()}
        render(status: 204, [] as JSON)
    }
}
