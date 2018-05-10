package tdn.api

import com.google.gson.JsonArray
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
        List<Node> nodes = User.executeQuery("match (f:User {username:${au.username}})<-[r]-(t:User) return t as data skip ${offset} limit ${max}")
        List<User> followers = new ArrayList<>()
        for (n in nodes) { followers.add(n as User) }

//        nodes = User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data")
//        List<User> following = new ArrayList<>()
//        for (n in nodes) { following.add(n as User) }

        JSONArray arr = prepareArray(followers, au)

        render arr as JSON
    }

    def followerCount(Long id) {
        User au = User.get(springSecurityService.principal.id)
        def followerCount = [followerCount: User.executeQuery("match (f: User {username: ${au.username}})<-[r]-(t: User) return t as data").size()]
        render followerCount as JSON
    }

    def save(Long id) {
        User u = User.get(id)
        User au = User.get(springSecurityService.principal.id)
        Follows follow = new Follows(from: au, to: u)
        follow.withTransaction {follow.save()}
        Notification.executeQuery("match (to:User{username:'${u.username}'}), (from:User{username:'${au.username}'}) " +
                "create (to)<-[r:NOTIFICATION{message:'Followed you', date: ${new Date().getTime()}, read: false," +
                " uri: '/profile/" + au.id + "'}]-(from)")
        au.withTransaction {au.save()}
//        u.addToFollowers(User.get(springSecurityService.principal.id))
//        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def delete(Long id) {
        User u = User.get(id)
        User au = User.get(springSecurityService.principal.id)
        //Follows.findByFromAndTo(au, u).delete()
        Follows.executeCypher("""MATCH (f:User {username:${au.username}})-[r:FOLLOWS]->(t:User {username:${u.username}}) delete r""")
        au.withTransaction {au.save()}
//        u.removeFromFollowers(User.get(springSecurityService.principal.id))
//        u.save(flush: true, failOnError: true)
        render u.followers as JSON
    }

    def following(Long id, Long offset, Long max) {
        User au = User.get(springSecurityService.principal.id)
        List<Node> nodes = User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data skip ${offset} limit ${max}")
        List<User> following = new ArrayList<>()
        for (n in nodes) { following.add(n as User) }

        JSONArray arr = prepareArray(following, au)
        render arr as JSON
    }

    def followingCount(Long id) {
        User au = User.get(springSecurityService.principal.id)
        def followingCount = [followingCount: User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data").size()]
        render followingCount as JSON
    }

    JSONArray prepareArray(List<User> userList, User au) {
        JSONArray arr = new JSONArray()
        userList.forEach {
            u -> def json = JSON.parse((u as JSON).toString())
                json.put("isFollowing", Follows.countByFromAndTo(au, u))
                arr.put(json)
        }
        return(arr)
    }
}
