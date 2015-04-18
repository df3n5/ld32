package ludumdare32
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.utils.Timer.Task

import com.badlogic.gdx.{Gdx, Screen}
import com.badlogic.gdx.graphics.g2d._
import com.badlogic.gdx.graphics.{Color, GL20, OrthographicCamera, Texture}
import com.badlogic.gdx.math.{MathUtils, Rectangle, Vector3}
import com.badlogic.gdx.utils.{Timer, TimeUtils}

import scala.collection.mutable.ArrayBuffer

class Iron(var lookingRight:Boolean) {
  var rot = 0.0f
  val rect = new Rectangle()
  rect.width = 20
  rect.height = 10
  var isAttacking = false

  def update(gameScreen: GameScreen): Unit = {
    val ironAngVelocity = 9.0f
    val ironSpeed = 9.0f
    if(lookingRight) {
      if (isAttacking) {
        rot -= ironAngVelocity
        rect.x += ironSpeed
      } else {
        rect.x = gameScreen.granny.x + gameScreen.granny.width * 0.75f
        rect.y = gameScreen.granny.y + gameScreen.granny.height * 0.29f
        rot = 0
      }
    } else {
      if (isAttacking) {
        rot += ironAngVelocity
        rect.x -= ironSpeed
      } else {
        rect.x = gameScreen.granny.x - gameScreen.granny.width * 0.50f
        rect.y = gameScreen.granny.y + gameScreen.granny.height * 0.29f
        rot = 0
      }
    }
  }
}

class GameScreen (game: LudumDareSkeleton) extends Screen {

  object WeaponType extends Enumeration {
    type WeaponType = Value
    val FryingPan, Iron = Value
  }
  import WeaponType._

  /*
  lazy val dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"))
  lazy val rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"))
  lazy val dropImage = new Texture(Gdx.files.internal("droplet.png"))
  */
  lazy val grannyImage = new Texture(Gdx.files.internal("granny.png"))
  lazy val fryingPanImage = new Texture(Gdx.files.internal("frying_pan.png"))

  lazy val ironImage = new Texture(Gdx.files.internal("iron.png"))
  lazy val ironingBoardImage = new Texture(Gdx.files.internal("ironing_board.png"))

  lazy val backgroundImage = new Texture(Gdx.files.internal("background_granny.png"))

  lazy val camera = new OrthographicCamera()
  lazy val game.batch = new SpriteBatch()
  //lazy var granny = new Sprite(grannyImage)
  lazy val granny = new Rectangle()
  //Weapons
  var weaponType = FryingPan
  var isAttacking = false
  lazy val fryingPan = new Rectangle()
  var fryingPanRot = 90.0f
  //lazy val activeIron = new Rectangle()
  //var activeIronRot = 0.0f
  var activeIron = new Iron(false)
  var irons = new ArrayBuffer[Iron]()
  // Shield
  var isDefending = false
  lazy val ironingBoard = new Rectangle()

  lazy val background = new Rectangle()

  //var raindrops = new ArrayBuffer[Rectangle]()
  //var lastDropTime : Long = 0
  //Animation
  val FRAME_COLS = 6
  val FRAME_ROWS = 5
  /*
  var walkAnimation : Animation = null
  var walkSheet : Texture = null
  var walkFrames : com.badlogic.gdx.utils.Array[TextureRegion] = null
  var spriteBatch : SpriteBatch = null
  */
  var currentFrame : TextureRegion = null
  var stateTime : Float = 0
  var lookingRight : Boolean = false
  //fonts
  var font : BitmapFont = null

  /*
  // start the playback of the background music immediately
  rainMusic.setLooping(true)
  rainMusic.play()
  */
  val gameHeight = 480
  val gameWidth = 800

  camera.setToOrtho(false, gameWidth, gameHeight)

  granny.width = 27
  granny.height = 51
  granny.x = gameWidth / 2 - granny.width / 2
  granny.y = 20

  fryingPan.width = 32
  fryingPan.height = 16

  ironingBoard.width = 20
  ironingBoard.height = 80

  background.width = gameWidth
  background.height = gameHeight

  //spawnRaindrop()

  /*
  //Animation stuff
  walkSheet = new Texture(Gdx.files.internal("animation_sheet.png"))
  val tmp = TextureRegion.split(walkSheet, walkSheet.getWidth()/FRAME_COLS, walkSheet.getHeight()/FRAME_ROWS)
  println("tmp.length " + tmp.length)
  println("tmp2.length " + tmp(0).length)
  walkFrames = new com.badlogic.gdx.utils.Array[TextureRegion](FRAME_COLS * FRAME_ROWS)
  for(i <- 0 to FRAME_ROWS-1) {
    for(j <- 0 to FRAME_COLS-1){
      println("i " + i + " j " + walkFrames.size)
      walkFrames.add(tmp(i)(j))
    }
  }
  walkAnimation = new Animation(0.025f, walkFrames)
  spriteBatch = new SpriteBatch()
  stateTime = 0f
  */

  //fonts
  font = new BitmapFont()
  font.setColor(Color.RED)
  var scheduled = false

  def fryingPanUpdate(): Unit = {
    if(weaponType == FryingPan) {
      val fryingPanSpeed = 9.0f
      if (lookingRight) {
        fryingPan.x = granny.x + granny.width * 0.95f
        fryingPan.y = granny.y + granny.height * 0.29f
        if (isAttacking) {
          fryingPanRot -= fryingPanSpeed
          if (fryingPanRot < 0.0f) {
            fryingPanRot = 90.0f
            isAttacking = false
          }
        } else {
          fryingPanRot = 90.0f
        }
      } else {
        fryingPan.x = granny.x - granny.width * 0.10f
        fryingPan.y = granny.y + granny.height * 0.29f
        if (isAttacking) {
          fryingPanRot += fryingPanSpeed
          if (fryingPanRot > 180.0f) {
            fryingPanRot = 90.0f
            isAttacking = false
          }
        } else {
          fryingPanRot = 90.0f
        }
      }
    }
  }

  def ironUpdate(): Unit = {
    if(weaponType == Iron) {
      if (activeIron != null && isAttacking) {
        activeIron.isAttacking = true
        irons += activeIron
        activeIron = null
        scheduled = true
        Timer.schedule(new Task {
          override def run(): Unit = {
            isAttacking = false
            scheduled = false
          }
        }, 1.0f)
      } else {
        if (activeIron == null && !isAttacking) {
          activeIron = new Iron(lookingRight)
        }/* else {
          if(activeIron==null && isAttacking && !scheduled) {
            activeIron = new Iron(lookingRight)
          }
        }*/
      }
    }
    if(activeIron != null) {
      activeIron.update(this)
      activeIron.lookingRight = lookingRight
    }
    irons.foreach { case iron => {
        iron.update(this)
      }
    }
  }

  def ironingBoardUpdate(): Unit = {
    ironingBoard.y = granny.y + granny.height * 0.0f
    if(lookingRight) {
      ironingBoard.x = granny.x + granny.width * 0.75f
    } else {
      ironingBoard.x = granny.x - granny.width * 0.50f
    }
  }

  override def render(delta:Float) {
    Gdx.gl.glClearColor(0, 0, 0.2f, 1)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    val lerp = 0.05f
    var diff = (granny.x - camera.position.x) * lerp
    val smallVal = 0.1
    if(diff < smallVal && diff > -smallVal) diff = 0.0f
    camera.position.set(camera.position.x + diff, camera.position.y, 0)
    camera.update()

    fryingPanUpdate
    ironUpdate
    ironingBoardUpdate

    game.batch.setProjectionMatrix(camera.combined)
    game.batch.begin()
    game.batch.draw(backgroundImage, background.x, background.y, background.width, background.height)
    //game.batch.draw(grannyImage, granny.x, granny.y)
    game.batch.draw(grannyImage, granny.x, granny.y, granny.width, granny.height)
    //game.batch.draw(fryingPanImage, fryingPan.x, fryingPan.y, fryingPan.width, fryingPan.height)
    if(isDefending) {
      game.batch.draw(ironingBoardImage, ironingBoard.x, ironingBoard.y, ironingBoard.height/2, ironingBoard.height/2, ironingBoard.width, ironingBoard.height, 1.0f, 1.0f, 0, 0, 0, ironingBoardImage.getWidth, ironingBoardImage.getHeight, false, false)
    } else {
      if(weaponType == FryingPan) game.batch.draw(fryingPanImage, fryingPan.x, fryingPan.y, fryingPan.height*0.1f, fryingPan.height/2, fryingPan.width, fryingPan.height, 1.0f, 1.0f, fryingPanRot, 0, 0, fryingPanImage.getWidth, fryingPanImage.getHeight, false, false)
      if(weaponType == Iron && activeIron != null) game.batch.draw(ironImage, activeIron.rect.x, activeIron.rect.y, activeIron.rect.width/2, activeIron.rect.height/2, activeIron.rect.width, activeIron.rect.height, 1.0f, 1.0f, activeIron.rot, 0, 0, ironImage.getWidth, ironImage.getHeight, lookingRight, false)
    }

    irons.foreach { case iron => game.batch.draw(ironImage, iron.rect.x, iron.rect.y, iron.rect.width / 2, iron.rect.height / 2, iron.rect.width, iron.rect.height, 1.0f, 1.0f, iron.rot, 0, 0, ironImage.getWidth, ironImage.getHeight, iron.lookingRight, false) }
    game.batch.end()

    //Handle input
    if(Gdx.input.isKeyPressed(Keys.NUM_1)) weaponType = FryingPan
    if(Gdx.input.isKeyPressed(Keys.NUM_2)) weaponType = Iron
    isDefending = Gdx.input.isKeyPressed(Keys.J)
      /*
    if(Gdx.input.isTouched) {
        val touchPos = new Vector3
        touchPos.set(Gdx.input.getX, Gdx.input.getY, 0)
        camera.unproject(touchPos)
        granny.x = touchPos.x - 64 / 2
      }
      */
    val grannyXSpeed = 300
    val grannyYSpeed = 200
    if(Gdx.input.isKeyPressed(Keys.A)) { granny.x -= grannyXSpeed * Gdx.graphics.getDeltaTime; lookingRight = false }
    if(Gdx.input.isKeyPressed(Keys.D)) { granny.x += grannyXSpeed * Gdx.graphics.getDeltaTime; lookingRight = true }
    if(Gdx.input.isKeyPressed(Keys.S)) granny.y -= grannyYSpeed * Gdx.graphics.getDeltaTime
    if(Gdx.input.isKeyPressed(Keys.W)) granny.y += grannyYSpeed * Gdx.graphics.getDeltaTime
    if(!isDefending && !isAttacking && Gdx.input.isKeyPressed(Keys.SPACE)) isAttacking = true

    // Stay within limits
    val limitPct = 0.75f
    if(granny.x < 0) granny.x = 0
    //if(granny.x > gameWidth - granny.width) granny.x = gameWidth - granny.width
    if(granny.y < 0) granny.y = 0
    if(granny.y > (gameHeight * limitPct) - granny.height) granny.y = (gameHeight * limitPct) - granny.height

    //background.x = 300 - granny.x;

    //camera.lookAt(granny.x, granny.y, 0)
    //camera.translate(0.1f, 0.0f, 0.0f)

    //if(TimeUtils.nanoTime - lastDropTime > 1000000000) spawnRaindrop()

    /*
    //TODO: Figure out a nicer way to do this
    var newRainDrops = new ArrayBuffer[Rectangle]()
    for(raindrop <- raindrops) {
      raindrop.y -= 200 * Gdx.graphics.getDeltaTime()
      var remove = false

      if(raindrop.overlaps(granny)) {
        dropSound.play()
        remove = true
      }

      if(raindrop.y + 64 < 0) remove = true

      if(! remove) newRainDrops += raindrop

    }
    raindrops = newRainDrops
    */

    /*
    game.batch.begin()
    game.batch.draw(grannyImage, granny.x, granny.y)
    for(raindrop <- raindrops) {
      batch.draw(dropImage, raindrop.x, raindrop.y)
    }
    game.batch.end()
    */

    /*
    // Animation stuff
    stateTime += Gdx.graphics.getDeltaTime()
    currentFrame = walkAnimation.getKeyFrame(stateTime, true)
    spriteBatch.begin()
    spriteBatch.draw(currentFrame, 50, 50)
    spriteBatch.end()
    */

    // font
    /*
    game.batch.begin()
    font.draw(game.batch, "Hello World", 200, 200)
    game.batch.end()
    */

  }

  override def dispose() {
    grannyImage.dispose()
    /*
    dropImage.dispose()
    dropSound.dispose()
    rainMusic.dispose()
    batch.dispose()
    */
  }

  def spawnRaindrop() {
    /*
    val raindrop = new Rectangle()
    raindrop.x = MathUtils.random(0, gameWidth-64)
    raindrop.y = gameHeight
    raindrop.width = 64
    raindrop.height = 64
    raindrops += raindrop
    lastDropTime = TimeUtils.nanoTime()
    */
  }

  override def resize(width: Int, height: Int): Unit = {}

  override def hide(): Unit = {}

  override def pause(): Unit = {}

  override def show(): Unit = {}

  override def resume(): Unit = {}
}
