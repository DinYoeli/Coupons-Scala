package models

import org.mongodb.scala.bson.ObjectId

/**
  * Admin class
  * userName:String = username of the admin
  * password:String = password of the admin
  * companyName:String = company name of the admin
  */
object Admin {
  def apply(userName:String, password:String, companyName:String): Admin =
    Admin(new ObjectId(), userName, password, companyName)
}

case class Admin(val _id:ObjectId, var userName:String, var password:String, var companyName:String){
  // constructor
  println("New Admin has just been created!")

  // getters and setters
  def getId():ObjectId = this._id
  def setUserName(newUserName:String):Unit = this.userName = newUserName
  def getUserName():String = this.userName
  def setPassword(newPassword:String):Unit = this.password = newPassword
  def getPassword():String = this.password
  def setCompanyName(newCompanyName:String):Unit = this.companyName = newCompanyName
  def getCompanyName():String = this.companyName
}
