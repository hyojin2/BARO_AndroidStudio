package my.application.ecogreen

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.disuse_waste_image.*
import kotlinx.android.synthetic.main.main_analyze_view.*
import java.io.File

class DisuseWasteActivity: AppCompatActivity() {
    private val CAMERA_PERMISSION_REQUEST = 1000
    private val GALLERY_PERMISSION_REQUEST = 1001
    private val FILE_NAME = "picture.jpg"
    private var uploadChooser : UploadChooser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.disuse_waste_image)

        setupListener()
    }

    private fun setupListener(){
        upload_image.setOnClickListener {
            //apply함수는 앞에 있는 부분 초기 설정할 때 활용하기 좋은 함수.
            uploadChooser = UploadChooser().apply {
                addNotifier(object : UploadChooser.UploadChooserNotifierInterface{
                    override fun cameraOnClick() {
                        Log.d("upload", "CameraOnClick")
                        // 카메라 권한 요청
                        checkCameraPermission()
                    }

                    override fun galleryOnClick() {
                        Log.d("upload", "galleryOnClick")
                        // 사진첩 권한 요청
                        checkGalleryPermission()
                    }

                    override fun typingOnClick() {
                        Log.d("upload", "typingOnClick")
                        startActivity(Intent(this@DisuseWasteActivity, DirectFillUpWaste::class.java))
                    }
                })
            }
            uploadChooser!!.show(supportFragmentManager, "")
        }

    }

    private fun checkCameraPermission(){
        if(PermissionUtil().requestPermission(
                this,
                CAMERA_PERMISSION_REQUEST,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ){
            openCamera()
        }
    }
    private fun checkGalleryPermission() {
        if(PermissionUtil().requestPermission(
                this,
                GALLERY_PERMISSION_REQUEST,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ){
            openGallery()
        }
    }

    private fun openGallery(){
        val intent = Intent().apply {
            //image 폴더의 전체 다
            setType("image/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }

        startActivityForResult(Intent.createChooser(intent, "Select a photo"), GALLERY_PERMISSION_REQUEST)
    }

    private fun openCamera(){
        //Uri는 경로 전체를 포함하는 의미(url은 인터넷 경로) -> photoUri: 사진을 저장할 uri 생성
        //getUriForFile(file을 위한 uri를 만들어주는 함수)
        val photoUri = FileProvider.getUriForFile(this,  "my.application.ecogreen.provider", createCameraFile())
//        val photoUri = FileProvider.getUriForFile(this,  applicationContext.packageName + ".provider", createCameraFile())

        //사진 찍은 후 결과값 이미지를 올려야 하므로 startActivityForResult 사용
        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                //찍은 결과물(EXTRA_OUTPUT)을 photoUri에 저장
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, CAMERA_PERMISSION_REQUEST
        )
    }

    //data: Intent? -> 코틀린에서 ?는 'null'일 수도 있다는 것을 의미
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            CAMERA_PERMISSION_REQUEST -> {
                //작업의 결과물을 떠나서 작업이 잘 되었는지 확인해주는 게 RESULT_OK. 잘못된 작업이면 굳이 결과물 가져오지 않아도 되니까
                if(resultCode != Activity.RESULT_OK) return
                val photoUri = FileProvider.getUriForFile(this,  "my.application.ecogreen.provider", createCameraFile())
                uploadImage(photoUri)

                //찍은 사진을 사진첩에 저장하고 싶으면 이 위치에 코드 추가하면 됨.

            }
            GALLERY_PERMISSION_REQUEST -> {
                //data가 null이 아니면 let 이하를 시작
                data?.let{
                    it.data?.let { it1 -> uploadImage(it1) }
                }
            }
        }
    }


    //실질적으로 사진첩이나 카메라에서 이미지를 받아오고, 마지막으로 받아온 이미지를 이미지뷰에 올려주는 것.(업로드할 이미지가 준비가 되어 있는 상황)
    private fun uploadImage(imageUri : Uri){
        //넘겨준 경로로 가서 사진을 받아옴. 사진은 비트맵으로 넘어옴.
        val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        uploaded_image.setImageBitmap(bitmap)
        uploadChooser?.dismiss()
    }

    private fun createCameraFile(): File {
        //dir은 핸드폰 사집첩 폴더
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(dir, FILE_NAME)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            GALLERY_PERMISSION_REQUEST -> {
                if(PermissionUtil().permissionGranted(requestCode, GALLERY_PERMISSION_REQUEST, grantResults)){
                    openGallery()
                }
            }
            CAMERA_PERMISSION_REQUEST -> {
                //내가 보낸 요청에 대한 답이 왔는지 확인하고, 허락받은 권한이 하나라도 있는지 확인(grantResults size로), 0보다 크다면 그 중 0번째를 꺼내서 grant 됐는지 확인
                if(PermissionUtil().permissionGranted(requestCode, CAMERA_PERMISSION_REQUEST, grantResults)){
                    openCamera()
                }
            }
        }
    }

}