/**
 * Мобільний фреймворк для роботи з функціями мобільних пристроїв
 * @author Андрій Будильников
 */
package com.sparky.xsparkyproject.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask

/**
 * представлення інтерфейсу для роботи з мобільними пристроями
 */
interface MobileDeviceManager {
    /**
     * отримати інформацію про пристрій
     *
     * @return інформація про пристрій
     */
    fun getDeviceInfo(): DeviceInfo

    /**
     * отримати інформацію про мережу
     *
     * @return інформація про мережу
     */
    fun getNetworkInfo(): NetworkInfoData

    /**
     * отримати інформацію про батарею
     *
     * @return інформація про батарею
     */
    fun getBatteryInfo(): BatteryInfo

    /**
     * отримати інформацію про пам'ять
     *
     * @return інформація про пам'ять
     */
    fun getStorageInfo(): StorageInfo

    /**
     * отримати інформацію про дисплей
     *
     * @return інформація про дисплей
     */
    fun getDisplayInfo(): DisplayInfo
}

/**
 * представлення інформації про пристрій
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val sdkVersion: Int,
    val deviceId: String,
    val serialNumber: String
)

/**
 * представлення інформації про мережу
 */
data class NetworkInfoData(
    val isConnected: Boolean,
    val connectionType: String,
    val ssid: String?,
    val signalStrength: Int,
    val ipAddress: String?
)

/**
 * представлення інформації про батарею
 */
data class BatteryInfo(
    val level: Int,
    val scale: Int,
    val percentage: Int,
    val isCharging: Boolean,
    val health: String,
    val temperature: Float
)

/**
 * представлення інформації про пам'ять
 */
data class StorageInfo(
    val totalInternalStorage: Long,
    val availableInternalStorage: Long,
    val totalExternalStorage: Long,
    val availableExternalStorage: Long,
    val totalRam: Long,
    val availableRam: Long
)

/**
 * представлення інформації про дисплей
 */
data class DisplayInfo(
    val width: Int,
    val height: Int,
    val density: Float,
    val refreshRate: Float,
    val brightness: Int
)

/**
 * представлення базової реалізації менеджера мобільних пристроїв
 */
class BaseMobileDeviceManager(private val context: Context) : MobileDeviceManager {
    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            osVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            deviceId = getDeviceId(),
            serialNumber = Build.SERIAL
        )
    }

    override fun getNetworkInfo(): NetworkInfoData {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true
        
        return NetworkInfoData(
            isConnected = isConnected,
            connectionType = activeNetwork?.typeName ?: "Unknown",
            ssid = null,
            signalStrength = 0,
            ipAddress = null
        )
    }

    override fun getBatteryInfo(): BatteryInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        
        return BatteryInfo(
            level = batteryLevel,
            scale = 100,
            percentage = batteryLevel,
            isCharging = false,
            health = "Good",
            temperature = 0.0f
        )
    }

    override fun getStorageInfo(): StorageInfo {
        val internalStorage = Environment.getDataDirectory()
        val externalStorage = Environment.getExternalStorageDirectory()
        
        val totalInternal = internalStorage.totalSpace
        val availableInternal = internalStorage.freeSpace
        val totalExternal = externalStorage.totalSpace
        val availableExternal = externalStorage.freeSpace
        
        return StorageInfo(
            totalInternalStorage = totalInternal,
            availableInternalStorage = availableInternal,
            totalExternalStorage = totalExternal,
            availableExternalStorage = availableExternal,
            totalRam = 0,
            availableRam = 0
        )
    }

    override fun getDisplayInfo(): DisplayInfo {
        val displayMetrics = context.resources.displayMetrics
        return DisplayInfo(
            width = displayMetrics.widthPixels,
            height = displayMetrics.heightPixels,
            density = displayMetrics.density,
            refreshRate = 60.0f,
            brightness = 50
        )
    }

    private fun getDeviceId(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            telephonyManager.deviceId ?: "unknown"
        } else {
            "permission_denied"
        }
    }
}

/**
 * представлення інтерфейсу для роботи з GPS
 */
interface GpsManager {
    /**
     * отримати поточне місцезнаходження
     *
     * @param timeout таймаут в мілісекундах
     * @return місцезнаходження
     */
    fun getCurrentLocation(timeout: Long = 10000): Location?

    /**
     * почати відстеження місцезнаходження
     *
     * @param interval інтервал в мілісекундах
     * @param listener обробник
     */
    fun startLocationTracking(interval: Long, listener: LocationListener)

    /**
     * зупинити відстеження місцезнаходження
     */
    fun stopLocationTracking()

    /**
     * перевірити дозвіл на доступ до місцезнаходження
     *
     * @return true, якщо дозвіл надано
     */
    fun isLocationPermissionGranted(): Boolean

    /**
     * отримати відстань між двома точками
     *
     * @param location1 перша точка
     * @param location2 друга точка
     * @return відстань в метрах
     */
    fun calculateDistance(location1: Location, location2: Location): Float
}

/**
 * представлення базової реалізації менеджера GPS
 */
class BaseGpsManager(private val context: Context) : GpsManager {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var isTracking = false

    override fun getCurrentLocation(timeout: Long): Location? {
        if (!isLocationPermissionGranted()) {
            return null
        }

        // Спроба отримати останнє відоме місцезнаходження
        var location: Location? = null
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        return location
    }

    override fun startLocationTracking(interval: Long, listener: LocationListener) {
        if (!isLocationPermissionGranted()) {
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0f, listener)
            isTracking = true
        }
    }

    override fun stopLocationTracking() {
        locationManager.removeUpdates(object : LocationListener {
            override fun onLocationChanged(location: Location) {}
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        })
        isTracking = false
    }

    override fun isLocationPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    override fun calculateDistance(location1: Location, location2: Location): Float {
        return location1.distanceTo(location2)
    }
}

/**
 * представлення інтерфейсу для роботи з камерою
 */
interface CameraManager {
    /**
     * зробити фото
     *
     * @param cameraId ідентифікатор камери
     * @return шлях до файлу фото
     */
    fun takePicture(cameraId: Int = 0): String?

    /**
     * записати відео
     *
     * @param cameraId ідентифікатор камери
     * @param duration тривалість в секундах
     * @return шлях до файлу відео
     */
    fun recordVideo(cameraId: Int = 0, duration: Int = 10): String?

    /**
     * отримати список доступних камер
     *
     * @return список камер
     */
    fun getAvailableCameras(): List<CameraInfo>

    /**
     * встановити параметри камери
     *
     * @param cameraId ідентифікатор камери
     * @param parameters параметри
     */
    fun setCameraParameters(cameraId: Int, parameters: CameraParameters)

    /**
     * отримати параметри камери
     *
     * @param cameraId ідентифікатор камери
     * @return параметри
     */
    fun getCameraParameters(cameraId: Int): CameraParameters
}

/**
 * представлення інформації про камеру
 */
data class CameraInfo(
    val id: Int,
    val name: String,
    val facing: String,
    val resolution: String
)

/**
 * представлення параметрів камери
 */
data class CameraParameters(
    val resolution: String,
    val focusMode: String,
    val flashMode: String,
    val zoom: Int
)

/**
 * представлення базової реалізації менеджера камери
 */
class BaseCameraManager(private val context: Context) : CameraManager {
    override fun takePicture(cameraId: Int): String? {
        // Це заглушка для зйомки фото
        return "/storage/emulated/0/Pictures/photo_${System.currentTimeMillis()}.jpg"
    }

    override fun recordVideo(cameraId: Int, duration: Int): String? {
        // Це заглушка для запису відео
        return "/storage/emulated/0/Movies/video_${System.currentTimeMillis()}.mp4"
    }

    override fun getAvailableCameras(): List<CameraInfo> {
        return listOf(
            CameraInfo(0, "Back Camera", "BACK", "1920x1080"),
            CameraInfo(1, "Front Camera", "FRONT", "1280x720")
        )
    }

    override fun setCameraParameters(cameraId: Int, parameters: CameraParameters) {
        // Це заглушка для встановлення параметрів камери
    }

    override fun getCameraParameters(cameraId: Int): CameraParameters {
        return CameraParameters(
            resolution = "1920x1080",
            focusMode = "AUTO",
            flashMode = "OFF",
            zoom = 0
        )
    }
}

/**
 * представлення інтерфейсу для роботи з медіа
 */
interface MediaManager {
    /**
     * відтворити аудіо
     *
     * @param filePath шлях до файлу
     * @return ідентифікатор програвача
     */
    fun playAudio(filePath: String): Int

    /**
     * зупинити відтворення аудіо
     *
     * @param playerId ідентифікатор програвача
     */
    fun stopAudio(playerId: Int)

    /**
     * відтворити відео
     *
     * @param filePath шлях до файлу
     */
    fun playVideo(filePath: String)

    /**
     * отримати список медіа файлів
     *
     * @param directory директорія
     * @param fileType тип файлу (AUDIO, VIDEO, IMAGE)
     * @return список файлів
     */
    fun getMediaFiles(directory: String, fileType: String): List<MediaFile>

    /**
     * отримати інформацію про медіа файл
     *
     * @param filePath шлях до файлу
     * @return інформація про файл
     */
    fun getMediaFileInfo(filePath: String): MediaFileInfo
}

/**
 * представлення медіа файлу
 */
data class MediaFile(
    val name: String,
    val path: String,
    val size: Long,
    val duration: Long,
    val fileType: String
)

/**
 * представлення інформації про медіа файл
 */
data class MediaFileInfo(
    val title: String,
    val artist: String?,
    val album: String?,
    val duration: Long,
    val bitrate: Int,
    val sampleRate: Int,
    val width: Int?,
    val height: Int?
)

/**
 * представлення базової реалізації менеджера медіа
 */
class BaseMediaManager(private val context: Context) : MediaManager {
    private val mediaPlayers = mutableMapOf<Int, MediaPlayer>()
    private var playerIdCounter = 0

    override fun playAudio(filePath: String): Int {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(filePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            
            playerIdCounter++
            mediaPlayers[playerIdCounter] = mediaPlayer
            return playerIdCounter
        } catch (e: Exception) {
            mediaPlayer.release()
            return -1
        }
    }

    override fun stopAudio(playerId: Int) {
        mediaPlayers[playerId]?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayers.remove(playerId)
        }
    }

    override fun playVideo(filePath: String) {
        // Це заглушка для відтворення відео
    }

    override fun getMediaFiles(directory: String, fileType: String): List<MediaFile> {
        val files = mutableListOf<MediaFile>()
        val dir = File(directory)
        
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && isMatchingFileType(file.name, fileType)) {
                    files.add(
                        MediaFile(
                            name = file.name,
                            path = file.absolutePath,
                            size = file.length(),
                            duration = 0,
                            fileType = fileType
                        )
                    )
                }
            }
        }
        
        return files
    }

    override fun getMediaFileInfo(filePath: String): MediaFileInfo {
        return MediaFileInfo(
            title = File(filePath).nameWithoutExtension,
            artist = null,
            album = null,
            duration = 0,
            bitrate = 0,
            sampleRate = 0,
            width = null,
            height = null
        )
    }

    private fun isMatchingFileType(fileName: String, fileType: String): Boolean {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (fileType.uppercase()) {
            "AUDIO" -> extension in listOf("mp3", "wav", "aac", "flac", "ogg")
            "VIDEO" -> extension in listOf("mp4", "avi", "mkv", "mov", "wmv")
            "IMAGE" -> extension in listOf("jpg", "jpeg", "png", "gif", "bmp")
            else -> false
        }
    }
}

/**
 * представлення інтерфейсу для роботи з сенсорами
 */
interface SensorManager {
    /**
     * отримати дані акселерометра
     *
     * @return дані акселерометра
     */
    fun getAccelerometerData(): SensorData

    /**
     * отримати дані гіроскопа
     *
     * @return дані гіроскопа
     */
    fun getGyroscopeData(): SensorData

    /**
     * отримати дані магнітного поля
     *
     * @return дані магнітного поля
     */
    fun getMagneticFieldData(): SensorData

    /**
     * отримати дані орієнтації
     *
     * @return дані орієнтації
     */
    fun getOrientationData(): OrientationData

    /**
     * почати відстеження сенсорів
     *
     * @param sensorTypes типи сенсорів
     * @param interval інтервал в мілісекундах
     * @param listener обробник
     */
    fun startSensorTracking(sensorTypes: List<String>, interval: Long, listener: SensorDataListener)

    /**
     * зупинити відстеження сенсорів
     */
    fun stopSensorTracking()
}

/**
 * представлення даних сенсора
 */
data class SensorData(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long
)

/**
 * представлення даних орієнтації
 */
data class OrientationData(
    val azimuth: Float,
    val pitch: Float,
    val roll: Float,
    val timestamp: Long
)

/**
 * представлення інтерфейсу для обробника даних сенсорів
 */
interface SensorDataListener {
    /**
     * обробити дані сенсора
     *
     * @param sensorType тип сенсора
     * @param data дані
     */
    fun onSensorDataChanged(sensorType: String, data: SensorData)

    /**
     * обробити дані орієнтації
     *
     * @param data дані орієнтації
     */
    fun onOrientationDataChanged(data: OrientationData)
}

/**
 * представлення базової реалізації менеджера сенсорів
 */
class BaseSensorManager(private val context: Context) : SensorManager {
    override fun getAccelerometerData(): SensorData {
        return SensorData(
            x = 0.0f,
            y = 0.0f,
            z = 9.8f,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun getGyroscopeData(): SensorData {
        return SensorData(
            x = 0.0f,
            y = 0.0f,
            z = 0.0f,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun getMagneticFieldData(): SensorData {
        return SensorData(
            x = 0.0f,
            y = 0.0f,
            z = 0.0f,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun getOrientationData(): OrientationData {
        return OrientationData(
            azimuth = 0.0f,
            pitch = 0.0f,
            roll = 0.0f,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun startSensorTracking(sensorTypes: List<String>, interval: Long, listener: SensorDataListener) {
        // Це заглушка для відстеження сенсорів
    }

    override fun stopSensorTracking() {
        // Це заглушка для зупинки відстеження сенсорів
    }
}

/**
 * представлення інтерфейсу для роботи з вібрацією
 */
interface VibrationManager {
    /**
     * вібрувати
     *
     * @param duration тривалість в мілісекундах
     */
    fun vibrate(duration: Long)

    /**
     * вібрувати з патерном
     *
     * @param pattern патерн вібрації
     * @param repeat повторення (-1 для одноразової вібрації)
     */
    fun vibrate(pattern: LongArray, repeat: Int)

    /**
     * зупинити вібрацію
     */
    fun cancel()

    /**
     * перевірити наявність вібратора
     *
     * @return true, якщо вібратор доступний
     */
    fun hasVibrator(): Boolean

    /**
     * перевірити наявність амплітудної вібрації
     *
     * @return true, якщо амплітудна вібрація доступна
     */
    fun hasAmplitudeControl(): Boolean
}

/**
 * представлення базової реалізації менеджера вібрації
 */
class BaseVibrationManager(private val context: Context) : VibrationManager {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    override fun vibrate(duration: Long) {
        if (hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        if (hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, repeat)
            }
        }
    }

    override fun cancel() {
        vibrator.cancel()
    }

    override fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }

    override fun hasAmplitudeControl(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.hasAmplitudeControl()
        } else {
            false
        }
    }
}

/**
 * представлення інтерфейсу для роботи з аудіо
 */
interface AudioManager {
    /**
     * встановити гучність
     *
     * @param streamType тип потоку
     * @param volume гучність
     * @param flags прапорці
     */
    fun setVolume(streamType: Int, volume: Int, flags: Int)

    /**
     * отримати гучність
     *
     * @param streamType тип потоку
     * @return гучність
     */
    fun getVolume(streamType: Int): Int

    /**
     * встановити режим аудіо
     *
     * @param mode режим
     */
    fun setAudioMode(mode: Int)

    /**
     * отримати режим аудіо
     *
     * @return режим
     */
    fun getAudioMode(): Int

    /**
     * увімкнути/вимкнути беззвучний режим
     *
     * @param silent беззвучний режим
     */
    fun setSilentMode(silent: Boolean)

    /**
     * перевірити беззвучний режим
     *
     * @return true, якщо беззвучний режим увімкнено
     */
    fun isSilentMode(): Boolean

    /**
     * увімкнути/вимкнути Bluetooth аудіо
     *
     * @param enabled увімкнено
     */
    fun setBluetoothAudio(enabled: Boolean)

    /**
     * перевірити Bluetooth аудіо
     *
     * @return true, якщо Bluetooth аудіо увімкнено
     */
    fun isBluetoothAudioEnabled(): Boolean
}

/**
 * представлення базової реалізації менеджера аудіо
 */
class BaseAudioManager(private val context: Context) : AudioManager {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    override fun setVolume(streamType: Int, volume: Int, flags: Int) {
        audioManager.setStreamVolume(streamType, volume, flags)
    }

    override fun getVolume(streamType: Int): Int {
        return audioManager.getStreamVolume(streamType)
    }

    override fun setAudioMode(mode: Int) {
        audioManager.mode = mode
    }

    override fun getAudioMode(): Int {
        return audioManager.mode
    }

    override fun setSilentMode(silent: Boolean) {
        audioManager.ringerMode = if (silent) android.media.AudioManager.RINGER_MODE_SILENT else android.media.AudioManager.RINGER_MODE_NORMAL
    }

    override fun isSilentMode(): Boolean {
        return audioManager.ringerMode == android.media.AudioManager.RINGER_MODE_SILENT
    }

    override fun setBluetoothAudio(enabled: Boolean) {
        // Це заглушка для увімкнення/вимкнення Bluetooth аудіо
    }

    override fun isBluetoothAudioEnabled(): Boolean {
        return false
    }
}

/**
 * представлення інтерфейсу для роботи з повідомленнями
 */
interface NotificationManager {
    /**
     * показати повідомлення
     *
     * @param title заголовок
     * @param message повідомлення
     * @param channelId ідентифікатор каналу
     * @param priority пріоритет
     */
    fun showNotification(title: String, message: String, channelId: String, priority: Int)

    /**
     * скасувати повідомлення
     *
     * @param notificationId ідентифікатор повідомлення
     */
    fun cancelNotification(notificationId: Int)

    /**
     * скасувати всі повідомлення
     */
    fun cancelAllNotifications()

    /**
     * створити канал повідомлень
     *
     * @param channelId ідентифікатор каналу
     * @param channelName ім'я каналу
     * @param description опис
     * @param importance важливість
     */
    fun createNotificationChannel(channelId: String, channelName: String, description: String, importance: Int)

    /**
     * встановити іконку повідомлення
     *
     * @param iconResId ідентифікатор ресурсу іконки
     */
    fun setNotificationIcon(iconResId: Int)

    /**
     * встановити звук повідомлення
     *
     * @param soundResId ідентифікатор ресурсу звуку
     */
    fun setNotificationSound(soundResId: Int)
}

/**
 * представлення базової реалізації менеджера повідомлень
 */
class BaseNotificationManager(private val context: Context) : NotificationManager {
    override fun showNotification(title: String, message: String, channelId: String, priority: Int) {
        // Це заглушка для показу повідомлення
    }

    override fun cancelNotification(notificationId: Int) {
        // Це заглушка для скасування повідомлення
    }

    override fun cancelAllNotifications() {
        // Це заглушка для скасування всіх повідомлень
    }

    override fun createNotificationChannel(channelId: String, channelName: String, description: String, importance: Int) {
        // Це заглушка для створення каналу повідомлень
    }

    override fun setNotificationIcon(iconResId: Int) {
        // Це заглушка для встановлення іконки повідомлення
    }

    override fun setNotificationSound(soundResId: Int) {
        // Це заглушка для встановлення звуку повідомлення
    }
}

/**
 * представлення інтерфейсу для роботи з контактами
 */
interface ContactManager {
    /**
     * отримати всі контакти
     *
     * @return список контактів
     */
    fun getAllContacts(): List<Contact>

    /**
     * знайти контакти за ім'ям
     *
     * @param name ім'я
     * @return список контактів
     */
    fun findContactsByName(name: String): List<Contact>

    /**
     * додати контакт
     *
     * @param contact контакт
     * @return true, якщо контакт додано
     */
    fun addContact(contact: Contact): Boolean

    /**
     * оновити контакт
     *
     * @param contact контакт
     * @return true, якщо контакт оновлено
     */
    fun updateContact(contact: Contact): Boolean

    /**
     * видалити контакт
     *
     * @param contactId ідентифікатор контакта
     * @return true, якщо контакт видалено
     */
    fun deleteContact(contactId: Long): Boolean

    /**
     * перевірити дозвіл на доступ до контактів
     *
     * @return true, якщо дозвіл надано
     */
    fun isContactsPermissionGranted(): Boolean
}

/**
 * представлення контакта
 */
data class Contact(
    val id: Long,
    val name: String,
    val phoneNumbers: List<String>,
    val emailAddresses: List<String>,
    val addresses: List<String>
)

/**
 * представлення базової реалізації менеджера контактів
 */
class BaseContactManager(private val context: Context) : ContactManager {
    override fun getAllContacts(): List<Contact> {
        // Це заглушка для отримання всіх контактів
        return emptyList()
    }

    override fun findContactsByName(name: String): List<Contact> {
        // Це заглушка для пошуку контактів за ім'ям
        return emptyList()
    }

    override fun addContact(contact: Contact): Boolean {
        // Це заглушка для додавання контакта
        return true
    }

    override fun updateContact(contact: Contact): Boolean {
        // Це заглушка для оновлення контакта
        return true
    }

    override fun deleteContact(contactId: Long): Boolean {
        // Це заглушка для видалення контакта
        return true
    }

    override fun isContactsPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * представлення інтерфейсу для роботи з SMS
 */
interface SmsManager {
    /**
     * відправити SMS
     *
     * @param phoneNumber номер телефону
     * @param message повідомлення
     * @return true, якщо SMS відправлено
     */
    fun sendSms(phoneNumber: String, message: String): Boolean

    /**
     * отримати всі SMS
     *
     * @return список SMS
     */
    fun getAllSms(): List<SmsMessage>

    /**
     * отримати SMS за номером телефону
     *
     * @param phoneNumber номер телефону
     * @return список SMS
     */
    fun getSmsByPhoneNumber(phoneNumber: String): List<SmsMessage>

    /**
     * видалити SMS
     *
     * @param messageId ідентифікатор повідомлення
     * @return true, якщо SMS видалено
     */
    fun deleteSms(messageId: Long): Boolean

    /**
     * перевірити дозвіл на відправку SMS
     *
     * @return true, якщо дозвіл надано
     */
    fun isSmsPermissionGranted(): Boolean
}

/**
 * представлення SMS повідомлення
 */
data class SmsMessage(
    val id: Long,
    val phoneNumber: String,
    val message: String,
    val timestamp: Long,
    val isSent: Boolean
)

/**
 * представлення базової реалізації менеджера SMS
 */
class BaseSmsManager(private val context: Context) : SmsManager {
    override fun sendSms(phoneNumber: String, message: String): Boolean {
        // Це заглушка для відправки SMS
        return true
    }

    override fun getAllSms(): List<SmsMessage> {
        // Це заглушка для отримання всіх SMS
        return emptyList()
    }

    override fun getSmsByPhoneNumber(phoneNumber: String): List<SmsMessage> {
        // Це заглушка для отримання SMS за номером телефону
        return emptyList()
    }

    override fun deleteSms(messageId: Long): Boolean {
        // Це заглушка для видалення SMS
        return true
    }

    override fun isSmsPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
    }
}