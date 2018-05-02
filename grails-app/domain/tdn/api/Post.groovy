package tdn.api

import com.tdnsecuredrest.User
import grails.gorm.annotation.Entity

@Entity
class Post {

    static belongsTo = [user: User]
    static hasMany = [likes: Like]

    String description
    String image
    Date date

    static constraints = {
        description blank: false
        image nullable: true
        date nullable: false
    }

    static mapWith = "neo4j"
}
