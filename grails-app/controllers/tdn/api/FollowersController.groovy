package tdn.api

import com.tdnsecuredrest.Follows
import com.tdnsecuredrest.User
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.neo4j.driver.v1.StatementResult


class FollowersController {

    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long offset, Long max) {
        List users = User.executeQuery("select u.followers from User u where u.id = ?",
                [springSecurityService.principal.id], [max: max, offset: offset])
        List<User> following = User.executeQuery("from User as u where :user in elements(u.followers)",
                [user: User.get(springSecurityService.principal.id)])
        JSONArray arr = new JSONArray()
        users.forEach {
            u -> def json = JSON.parse((u as JSON).toString())
                json.put("isFollowing", following.contains(u))
                arr.put(json)
        }
        render arr as JSON
    }

    def followerCount(Long id) {
        def followerCount = [followerCount: User.get(id).followers.size()]
        render followerCount as JSON
    }

    def save(Long id) {
        User u = User.get(id)
        Follows follow = new Follows(from: User.get(springSecurityService.principal.id), to: u)
        follow.withTransaction {follow.save()}
//        u.addToFollowers(User.get(springSecurityService.principal.id))
//        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def delete(Long id) {
        User u = User.get(id)
        User au = User.get(springSecurityService.principal.id)
        Follows.executeCypher("""MATCH (f:User {username:${au.username}})-[r]->(t:User {username:${u.username}}) delete r""")
        au.withTransaction {au.save()}
//        u.removeFromFollowers(User.get(springSecurityService.principal.id))
//        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def following(Long id, Long offset, Long max) {
        List<User> users = User.executeQuery("from User as u where :user in elements(u.followers)", [user: User.get(id)], [offset: offset, max: max])
        JSONArray arr = new JSONArray()
        users.forEach {
            u -> def json = JSON.parse((u as JSON).toString())
                json.put("isFollowing", true)
                arr.put(json)
        }
        render arr as JSON
    }

    def followingCount(Long id) {
        def followingCount = [followingCount: User.executeQuery("from User as u where :user in elements(u.followers)", [user: User.get(id)]).size()]
        render followingCount as JSON
    }
}
