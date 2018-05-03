package tdn.api

import com.tdnsecuredrest.User
import grails.converters.JSON
import org.codehaus.groovy.classgen.asm.sc.StaticTypesBinaryExpressionMultiTypeDispatcher
import org.grails.web.json.JSONArray
import org.joda.time.DateTimeZone
import org.neo4j.driver.v1.StatementResult
import org.neo4j.driver.v1.types.Node

import javax.annotation.security.RolesAllowed

class PostController {

//    static notifMessage =
    static responseFormats = ['json', 'xml']
    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long id, Long max, Long offset) {
        User u = User.get(id)
        List<Node> nodes = Post.executeQuery("match (p: Post)<-[r: POSTED]-(u: User {username:${u.username}}) return p, u order by p.date desc", [max: max, offset: offset])
        List<Node> userNodes = nodes["u"]
        List<Node> postNodes = nodes["p"]
        List<Post> postList = new ArrayList<>()
        List<User> userList = new ArrayList<>()
        for (n in postNodes) { postList.add(n as Post) }
        for (n in userNodes) { userList.add(n as User) }
        JSONArray arr = postListToJSONArray(postList, userList)
        render arr as JSON
    }

    def count() {
        User au = User.get(springSecurityService.principal.id)
        def count = [postCount: Post.findAll("match (p: Post)<-[r: POSTED]-(u:User) where ((u)<-[:FOLLOWS]-(:User {username:${au.username}})" + \
                                                "or u.username = ${au.username}) return p").size()]
        render count as JSON
    }

    def post(Long id) {
        Post post = Post.get(id)
        JSONArray arr = postListToJSONArray([post].toList(), [Posted.findByTo(post).from].toList())
        render arr[0] as JSON
    }

    def posts(Long offset, Long max) {
        User au = User.get(springSecurityService.principal.id)
        List<Node> nodes = Post.executeQuery("match (p: Post)<-[r:POSTED]-(u:User) where ((u)<-[:FOLLOWS]-(:User {username:${au.username}})" +
                                             "or u.username = ${au.username}) return p, u order by p.date desc", [max: max, offset: offset])
        List<Node> userNodes = nodes["u"]
        List<Node> postNodes = nodes["p"]
        println(postNodes)
        List<Post> postList = new ArrayList<>()
        List<User> userList = new ArrayList<>()
        for (n in postNodes) { postList.add(n as Post) }
        for (n in userNodes) { userList.add(n as User) }
        JSONArray arr = postListToJSONArray(postList, userList)
        render arr as JSON
    }

    def save(Post post) {
        User au = User.get(springSecurityService.principal.id)
        post.date = new Date()
        Posted posted = new Posted(from: au, to: post)
        posted.withTransaction { posted.save() }
        //StatementResult statementResult = Post.executeCypher("match (u:User {username:${au.username}}) create (u)-[r:Posted]->(p:Post {description: ${post.description}, image: ${post.image}, date: ${post.date.getTime()}}) return p as data")
        //au.withTransaction { au.save() }
        // AFTER ALL SETTLED DOWN, WE DO NOTIFICATIONS sendNotifications(post.user, post.user.followers.toList(), 'Posted new content', post.date, post)
        JSONArray arr = postListToJSONArray([post].toList(), [au].toList())
        render(status: 201, arr[0] as JSON)
    }

    def like(Likes likeObj) {
        User au = User.get(springSecurityService.principal.id)
        likeObj.from = au

        if (Likes.countByFromAndTo(au, likeObj.to) >= 1) {
            Likes.withTransaction { Likes.findByFromAndTo(au, likeObj.to).delete() }
        } else {
            likeObj.withTransaction { likeObj.save() }
            // sendNotifications(au, au.followers.toList(), 'Liked your post', new Date(), post)
        }

        JSONArray arr = postListToJSONArray([likeObj.to].toList(), [Posted.findByTo(likeObj.to).from].toList())
        render(status: 200, arr[0] as JSON)
    }

    JSONArray postListToJSONArray(List<Post> postList, List<User> userList) {
        User au = User.get(springSecurityService.principal.id)
        JSONArray arr = new JSONArray()
        int at = 0
        postList.forEach {
            p -> def json = JSON.parse((p as JSON).toString())
                json.put("id", p.id)
                json.put("hasLiked", Likes.countByFromAndTo(au, p) > 0)
                json.put("likeCount", Likes.countByTo(p))
                json.put("user", User.get(userList[at].id))
                arr.put(json)
            at += 1
        }
        return(arr)
    }

    void sendNotifications(User from, List<User> list, String notifMessage, Date date, Post p) {
        list.each {
            Notification n = new Notification(message: notifMessage, date: date,
                    read: false, destUser: it, fromUser: from, uri: '/post/' + p.id)
            n.save(flush: true, failOnError: true)
        }
    }
}