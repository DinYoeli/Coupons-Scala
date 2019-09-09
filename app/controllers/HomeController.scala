package controllers

import java.nio.file.Paths
import javax.inject._
import models._
import org.bson.types.ObjectId
import play.api.Environment
import play.api.cache.SyncCacheApi
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cache: SyncCacheApi, cc: ControllerComponents, af:AssetsFinder, env:Environment) extends AbstractController(cc) {
  // controller constructor
  // instance of the model
  val _Instance:ICouponsDAO = CouponsDAO
  // default list of coupons to be used if there are no coupons in the db
  val defaultCouponsList = List(new Coupon(null,"Default Coupon","Default Coupon","default_coupon.png",null),
                                new Coupon(null,"קופון לדוגמא", "קופון לדוגמא", "default_coupon_hebrew.png", null))
  // the coupons list to be used throughout the controller
  var couponsList:List[Coupon] = List[Coupon]()
  // the key used to insert the admin user to the cache
  val cacheAdminKey = "connected"
  // a string to send to the user
  var msgToClient = ""
  // a boolean that dictates if a admin is connected or not
  var connectedAdmin = false
  // user form data handling
  val userForm = Form.apply(
    tuple(
      "username" -> text,
      "password" -> text,
      "company" -> text
    )
  )
  // coupon form data handling
  val couponForm = Form.apply(
    tuple(
      "couponName" -> text,
      "couponDesc" -> text
    )
  )
  // login form data handling
  val loginForm = Form.apply(
    tuple(
      "username" -> text,
      "password" -> text,
    )
  )

  /**
    * Index function
    * gets the list of all the coupons and sends it to the view to show
   */
  def index = Action { implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null){
      msgToClient = "Welcome Back " + admin.getUserName() + "!"
      connectedAdmin = true
      println("cache exists " + admin.getUserName())
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }

    else {
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * checks if the admin is connected and if yes then
    * redirects him to his private management page
    */
  def management = Action{ implicit  request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin == null || admin.getId() == null){
      // user not connected
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }

    else {
      couponsList = _Instance.getAdminCouponsList(admin.getId())
      if (couponsList == null || couponsList.isEmpty)
        couponsList = defaultCouponsList
      msgToClient = "Hi " + admin.getUserName() + ", Welcome To Your Personal Space :)"
      Ok(views.html.management.render(couponsList, msgToClient))
    }
  }

  /**
    * A simple redirect function for admin registration
   */
  def register = Action{ implicit request =>
    Ok(views.html.register())
  }

  /**
    * function that handles the form submission of new user registration
    * tries to pull the data from the form and if the data is ok
    * then checks if the admin already exists in the DB
    * if not then adds it and sets a new session
   */
  def registerPost = Action{ implicit request =>
    // try to get the admin instance from cache, if succeeds then user already registered
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    msgToClient = "Welcome New User!"
    connectedAdmin = false

    if (admin != null){
      msgToClient = "You're already connected.."
      connectedAdmin = true
      println("user already registered")
    }

    else {
      val formData = userForm.bindFromRequest

      if (formData.hasErrors) {
        msgToClient = "ERROR: Registration failed! Please try again.."
        println("Bad Request!!!")
      }

      else {
        val data = formData.get
        if (!_Instance.isAdminExist(data._1, data._2)) {
          _Instance.addAdmin(data._1, data._2, data._3)
          val adminId = _Instance.getAdminId(data._1)
          msgToClient = "Welcome " + data._1 + "!"
          connectedAdmin = true
          // adding the new admin instance to cache
          cache.set(cacheAdminKey, _Instance.getAdminAsInstance(adminId))
        }
        else{
          msgToClient = "ERROR: Registration failed! Please try again.."
        }
      }
    }
    Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
  }

  /**
    * A simple redirect function for admin login
   */
  def login = Action{ implicit request =>
    Ok(views.html.login())
  }

  /**
    * function that handles the form submission of admin login
    * tries to pull the data from the form and if the data is ok
    * then checks if the admin does exists in the DB
    * if not then shows a message
    * else creates a new session
   */
  def loginPost = Action{ implicit request =>
    val formData = loginForm.bindFromRequest

    msgToClient = "Welcome New User!"
    connectedAdmin = false

    if (formData.hasErrors){
      println("Bad Request!!!")
      msgToClient = "ERROR: Login Failed! Please try again!"
    }
    else{
      val data = formData.get
      if (_Instance.isAdminExist(data._1, data._2)){
        println("Login successful!")
        val adminId = _Instance.getAdminId(data._1)
        msgToClient = "Welcome Back " + data._1 + "!"
        connectedAdmin = true
        // adding the new admin instance to cache
        cache.set(cacheAdminKey, _Instance.getAdminAsInstance(adminId))
      }
        // no user or wrong credentials
      else{
        println("Bad login!")
        msgToClient = "ERROR: Login Failed! Please try again!"
      }
    }

    Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
  }

  /**
    * A simple function that returns the user to the index html with new session (invalidates the old one)
  */
  def logout = Action{ implicit request =>
    cache.remove(cacheAdminKey)
    msgToClient = "Welcome New User!"
    connectedAdmin = false
    Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
  }

  /**
    * a simple redirect function for new coupon creation
    */
  def newCoupon = Action{ implicit request =>
    Ok(views.html.addCoupon())
  }

  /**
    * function that handles the form submission of new coupon creating
    * first tries to get the connected admin id, to use it for the creation
    * then tries to pull the data from the form and if the data is ok
    * inserts the coupon into the DB with the admin id and refreshes the admin coupon list to show
    */
  def newCouponPost = Action{ implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null){
      val formData = couponForm.bindFromRequest

      if (formData.hasErrors){
        println("Bad Request!!!")
        couponsList = _Instance.getAdminCouponsList(admin.getId())
        msgToClient = "There was an error adding that coupon :( Please try again..."
        Ok(views.html.management.render(couponsList, msgToClient))
      }

      else{
        val data = formData.get
        _Instance.addCoupon(data._1, data._2, "default_coupon.png", admin.getId())
        couponsList = _Instance.getAdminCouponsList(admin.getId())
        msgToClient = "Hi " + admin.getUserName() + ", You just added a new coupon! :)"
        Ok(views.html.management.render(couponsList, msgToClient))
      }
    }

    else{
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * function that handles the call to delete a coupon from the DB
    * @param couponId = the id of the coupon chosen by the user in the html file
    */
  def delCoupon(couponId:String) = Action{ implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null && couponId != null){
      _Instance.delCoupon(new ObjectId(couponId))
      couponsList = _Instance.getAdminCouponsList(admin.getId())
      msgToClient = "Hi " + admin.getUserName() + ", that coupon has been deleted!"
      Ok(views.html.management.render(couponsList, msgToClient))
    }

    else{
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * function that redirects the user to the html with the modify coupon form
    * @param couponId = the id of the coupon chosen by the user in the html file
    */
  def modifyCoupon(couponId:String) = Action{ implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null && couponId != null){
      Ok(views.html.couponsDetails.render(couponId))
    }

    else {
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * function that handles the form submission of modifying a coupon
    * changes a coupon name and details based on the data given by the user
    * if the field is empty then makes no changes
    * @param couponId = the id of the coupon chosen by the user in the html file
    */
  def changeCouponPost(couponId:String) = Action{ implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null && couponId != null){
      val formData = couponForm.bindFromRequest

      if (formData.hasErrors){
        msgToClient = "ERROR: Modifying coupon failed! Please try again.."
        println("Bad Request!!!")
      }

      else{
        val data = formData.get
        if (data._1 != ""){
          _Instance.modifyCouponName(new ObjectId(couponId), data._1)
        }

        if (data._2 != ""){
          _Instance.modifyDescriptionName(new ObjectId(couponId), data._2)
        }
        msgToClient = "Hi " + admin.getUserName() + ", the coupon has been updated!"
      }

      couponsList = _Instance.getAdminCouponsList(admin.getId())
      Ok(views.html.management.render(couponsList, msgToClient))
    }
    else {
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * function that redirects the user to the html with the upload image file submission
    * @param couponId = the id of the coupon chosen by the user in the html file
    */
  def uploadPic(couponId:String) = Action{ implicit request =>
    // try to get the admin instance from cache
    val admin:Admin = cache.get[Admin](cacheAdminKey)match{
      case Some(a) => a
      case None => null
    }

    if (admin != null && couponId != null){
      Ok(views.html.uploadPic.render(couponId))
    }

    else {
      msgToClient = "Welcome New User!"
      connectedAdmin = false
      Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
    }
  }

  /**
    * function that handles the upload of a new coupon image
    * gets the file the user submitted and saves it in /public/images folder
    * @param couponId = the id of the coupon chosen by the user in the html file
    */
  def uploadPicPost(couponId:String) = Action(parse.multipartFormData){ implicit request =>
    request.body.file("picture").map{ picture =>
      // try to get the admin instance from cache
      val admin:Admin = cache.get[Admin](cacheAdminKey)match{
        case Some(a) => a
        case None => null
      }

      if (admin != null && couponId != null){
        val filename = Paths.get(picture.filename).getFileName
        val path = env.rootPath + af.assetsBasePath + "/images/" + filename
        println("THE FILE NAME " + path)

        picture.ref.moveTo(Paths.get(path), replace = true)
        _Instance.modifyPicPathName(new ObjectId(couponId), filename.toString)

        couponsList = _Instance.getAdminCouponsList(admin.getId())
        msgToClient = "Hi " + admin.getUserName() + ", the coupon picture has been updated!"
        Ok(views.html.management.render(couponsList, msgToClient))
      }
      else{
        msgToClient = "Welcome New User!"
        connectedAdmin = false
        Ok(views.html.index.render(couponsListFunc(), connectedAdmin, msgToClient))
      }
    }.getOrElse{
      msgToClient = "ERROR: Failed to get the file! Please try again.."
      Ok(views.html.management.render(couponsList, msgToClient))
    }
  }

  /**
    * function that handles the update of the coupons list
    * if the list is empty then uses the default one
    * @return List[Coupon] = lists of all the coupons in the DB or default list
    */
  def couponsListFunc():List[Coupon]={
    couponsList = _Instance.getAllCoupons()
    if (couponsList.isEmpty)
      couponsList = defaultCouponsList

    couponsList
  }
}