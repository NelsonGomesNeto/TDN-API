package tdn.api

import com.tdnsecuredrest.User
import grails.converters.JSON
import org.codehaus.groovy.classgen.asm.sc.StaticTypesBinaryExpressionMultiTypeDispatcher
import org.grails.web.json.JSONArray
import org.joda.time.DateTimeZone
import org.neo4j.driver.v1.StatementResult

import javax.annotation.security.RolesAllowed

class PostController {

//    static notifMessage =
    static responseFormats = ['json', 'xml']
    transient springSecurityService
    static transients = ['springSecurityService']

    def index(Long id, Long max, Long offset) {
        List<Post> list = Post.findAll("from Post as p where p.user = ? order by p.date desc", [User.get(id)], [max: max, offset: offset])
        JSONArray arr = postListToJSONArray(list)
        render arr as JSON
    }

    def count() {
        def count = [postCount: Post.findAll("from Post as p where p.user.id in (select u.id from User as u where :user in elements(u.followers)) or p.user = :user order by p.date desc", [user: User.get(springSecurityService.principal.id)]).size()]
        render count as JSON
    }

    def post(Long id) {
        JSONArray arr = postListToJSONArray([Post.get(id)].toList())
        render arr[0] as JSON
    }

    def posts(Long offset, Long max) {
        List<Post> list = Post.findAll("from Post as p where p.user.id in (select u.id from User as u where :user in elements(u.followers)) or p.user = :user order by p.date desc", [user: User.get(springSecurityService.principal.id)], [offset: offset, max: max])
        JSONArray arr = postListToJSONArray(list)
        render arr as JSON
    }

    def save(Post post) {
        User au = User.get(springSecurityService.principal.id)
        post.user = au
        post.date = new Date()
        post.likes = []
        //post.withTransaction { post.save() }
        StatementResult statementResult = Post.executeCypher("match (u:User {username:${au.username}}) create (u)-[r:Posted]->(p:Post {description: ${post.description}, image: ${post.image}, date: ${post.date.getTime()}}) return p as data")
        au.withTransaction { au.save() }
        println(statementResult)
        // AFTER ALL SETTLED DOWN, WE DO NOTIFICATIONS sendNotifications(post.user, post.user.followers.toList(), 'Posted new content', post.date, post)
        JSONArray arr = postListToJSONArray([post].toList())
        render(status: 201, arr[0] as JSON)
    }

    def like(Like likeObj) {
        User au = User.get(springSecurityService.principal.id)
        likeObj.user = au
        Post post = Post.get(likeObj.post.id)

        if (Like.countByUserAndPost(au, post) >= 1) {
            Like.findByUserAndPost(au, post).delete(flush: true)
        } else {
            likeObj.save(flush: true, failOnError: true)
            sendNotifications(au, au.followers.toList(), 'Liked your post', new Date(), post)
        }

        JSONArray arr = postListToJSONArray([post].toList())
        render(status: 200, arr[0] as JSON)
    }

    JSONArray postListToJSONArray(List<Post> list) {
        User au = User.get(springSecurityService.principal.id)
        JSONArray arr = new JSONArray()
        list.forEach {
            p -> def json = JSON.parse((p as JSON).toString())
                json.put("hasLiked", p.likes.collect(){item -> (item.user.id == au.id)}.contains(true))
                json.put("likeCount", p.likes.size())
                json.remove("likes")
                arr.put(json)
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