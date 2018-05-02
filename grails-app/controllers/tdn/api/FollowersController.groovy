package tdn.api

import com.tdnsecuredrest.Follows
import com.tdnsecuredrest.User
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.neo4j.driver.v1.StatementResult
import org.neo4j.driver.v1.types.Node


class FollowersController {

    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long offset, Long max) {
        User au = User.get(springSecurityService.principal.id)

        List<Node> nodes = User.executeQuery("match (f:User {username:${au.username}})<-[r]-(t:User) return t as data", [offset: offset, max: max])
        List<User> followers = new ArrayList<>()
        for (n in nodes) { followers.add(n as User) }

        nodes = User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data")
        List<User> following = new ArrayList<>()
        for (n in nodes) {
            following.add(n as User)
        }

        JSONArray arr = new JSONArray()
        followers.forEach {
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
        User au = User.get(springSecurityService.principal.id)
        List<Node> nodes = User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data", [offset: offset, max: max])
        List<User> following = new ArrayList<>()
        for (n in nodes) {
            following.add(n as User)
        }

        JSONArray arr = new JSONArray()
        following.forEach {
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
