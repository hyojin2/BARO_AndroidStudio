package my.application.ecogreen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_home.*
import kotlinx.android.synthetic.main.select_service_dialog.*

class MainHomeActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_home)

        reuse_waste.setOnClickListener {
            startActivity(Intent(this, ReuseWasteActivity::class.java))
        }
        disuse_waste.setOnClickListener {
            startActivity(Intent(this, DisuseWasteActivity::class.java))
        }
        account_setting.setOnClickListener {
            startActivity(Intent(this, AccountingSettingActivity::class.java))
        }
    }

}