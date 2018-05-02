package tdn.api

import com.google.gson.Gson
import com.tdnsecuredrest.User
import grails.converters.JSON
import org.grails.web.json.JSONArray
import org.neo4j.driver.v1.Record
import org.neo4j.driver.v1.StatementResult
import org.neo4j.driver.v1.types.Node

class UsersController {

    static responseFormats = ['json', 'xml']
    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long offset, Long max) {
        User au = User.get(springSecurityService.principal.id)
        List<User> list =  User.list(offset: offset, max: max)
        JSONArray arr = new JSONArray()
        List<Node> nodes = User.executeQuery("match (f: User {username: ${au.username}})-[r]->(t: User) return t as data")
        List<User> following = new ArrayList<>()
        for (n in nodes) {
            following.add(n as User)
        }
        list.forEach {
            u -> def json = JSON.parse((u as JSON).toString())
                json.put("isFollowing", following.contains(u))
                arr.put(json)
        }
        render arr as JSON
    }

    def count() {
        def count = [userCount: User.count]
        render count as JSON
    }
}
