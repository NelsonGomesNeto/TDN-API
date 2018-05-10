package tdn.api

import com.tdnsecuredrest.User
import grails.neo4j.Relationship
import grails.gorm.annotation.Entity

@Entity
class Notification implements Relationship<User, User> {

    //static belongsTo = [destUser: User, fromUser: User]

    String message
    String uri
    boolean read
    Date date

    static constraints = {
    }

    static mapWith = "neo4j"
}
