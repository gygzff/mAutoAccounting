/*
 * Copyright (C) 2023 ankio(ankio@ankio.net)
 * Licensed under the Apache License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-3.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.ankio.auto.ui.fragment.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.text.HtmlCompat
import com.quickersilver.themeengine.ThemeChooserDialogBuilder
import com.quickersilver.themeengine.ThemeEngine
import com.quickersilver.themeengine.ThemeMode
import net.ankio.auto.App
import net.ankio.auto.R
import net.ankio.auto.databinding.FragmentSetting2Binding
import net.ankio.auto.utils.ActiveUtils
import net.ankio.auto.utils.AppUtils
import net.ankio.auto.utils.CustomTabsHelper
import net.ankio.auto.utils.LocaleDelegate
import net.ankio.auto.utils.SpUtils
import net.ankio.utils.LangList
import java.util.Locale


class Setting2Fragment:Fragment() {
    private lateinit var binding: FragmentSetting2Binding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetting2Binding.inflate(layoutInflater)
        initView()
        return binding.root
    }

    private fun initView(){
        //匿名分析
        initCrash()
        //语言配置
        initLanguage()

        //主题
        initTheme()

        //调试模式
       initDebug()
    }

    private fun initCrash(){
        SpUtils.getBoolean("sendToAppCenter",true).apply { binding.analyze.isChecked = this }
        binding.AnalyzeView.setOnClickListener {
            binding.analyze.isChecked = !binding.analyze.isChecked
            SpUtils.putBoolean("sendToAppCenter",binding.analyze.isChecked )
        }
        binding.analyze.setOnCheckedChangeListener { _, isChecked ->  SpUtils.putBoolean("sendToAppCenter",isChecked ) }
    }

    private fun initLanguage(){
        val languages: ArrayList<String> = ArrayList()

        LangList.LOCALES.forEach {
            if (it.equals("SYSTEM")) {
                languages.add(getString(R.string.lang_follow_system))
            }else{
                val locale = Locale.forLanguageTag(it)
                languages.add(
                    HtmlCompat.fromHtml(locale.getDisplayName(locale), HtmlCompat.FROM_HTML_MODE_LEGACY)
                        .toString())
            }
        }

        SpUtils.getString("setting_language","SYSTEM").apply { binding.settingLangDesc.text = if (this == "SYSTEM") getString(R.string.lang_follow_system) else Locale.forLanguageTag(this).displayName }



        val listPopupWindow = ListPopupWindow(requireContext(), null)

        listPopupWindow.anchorView =  binding.settingLangDesc

        val adapter = ArrayAdapter(requireContext(), R.layout.list_popup_window_item,  languages)
        listPopupWindow.setAdapter(adapter)
        listPopupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT

        listPopupWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val value = LangList.LOCALES[position]
            binding.settingLangDesc.text = languages[position]
            SpUtils.putString("setting_language",value)
            val locale = AppUtils.getLocale(value)
            if(locale===null)return@setOnItemClickListener
            listPopupWindow.dismiss()
            LocaleDelegate.defaultLocale = AppUtils.getLocale()
            recreateInit()
        }

        binding.settingLang.setOnClickListener{ listPopupWindow.show() }
        //翻译
        binding.settingTranslate.setOnClickListener{
            CustomTabsHelper.launchUrlOrCopy(requireContext(), getString(R.string.translation_url))
        }

    }

    private fun initTheme(){
        val color = ThemeEngine.getInstance(requireContext()).staticTheme.primaryColor

        binding.colorSwitch.setCardBackgroundColor(requireActivity().getColor(color))

        binding.settingTheme.setOnClickListener {
            ThemeChooserDialogBuilder(requireContext())
                .setTitle(R.string.choose_theme)
                .setPositiveButton(getString(R.string.ok)) { _, theme ->
                    ThemeEngine.getInstance(requireContext()).staticTheme = theme
                    recreateInit()
                }
                .setNegativeButton(getString(R.string.close))
                .setNeutralButton(getString(R.string.default_theme)) { _, _ ->
                    ThemeEngine.getInstance(requireContext()).resetTheme()
                    recreateInit()
                }
                .setIcon(R.drawable.ic_theme)
                .create()
                .show()
        }

        val stringList = arrayListOf(getString(R.string.always_off),getString(R.string.always_on),getString(R.string.lang_follow_system))

        binding.settingDarkTheme.text = when(ThemeEngine.getInstance(requireContext()).themeMode){
            ThemeMode.DARK -> stringList[1]
            ThemeMode.LIGHT -> stringList[0]
            else  -> stringList[2]
        }

        val listPopupThemeWindow = ListPopupWindow(requireContext(), null)

        listPopupThemeWindow.anchorView =  binding.settingDarkTheme

        listPopupThemeWindow.setAdapter(ArrayAdapter(requireContext(), R.layout.list_popup_window_item,  stringList))
        listPopupThemeWindow.width = WindowManager.LayoutParams.WRAP_CONTENT

        listPopupThemeWindow.setOnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            binding.settingDarkTheme.text = stringList[position]
            ThemeEngine.getInstance(requireContext()).themeMode = when(position){
                1 -> ThemeMode.DARK
                0 -> ThemeMode.LIGHT
                else -> ThemeMode.AUTO
            }
            listPopupThemeWindow.dismiss()
        }

        binding.settingDark.setOnClickListener{ listPopupThemeWindow.show() }


        binding.alwaysDark.isChecked = ThemeEngine.getInstance(requireContext()).isTrueBlack
        binding.settingUseDarkTheme.setOnClickListener {
            ThemeEngine.getInstance(requireContext()).isTrueBlack = !binding.alwaysDark.isChecked
            binding.alwaysDark.isChecked = !binding.alwaysDark.isChecked
        }
        binding.alwaysDark.setOnCheckedChangeListener { _, isChecked ->  ThemeEngine.getInstance(requireContext()).isTrueBlack =  isChecked //;recreateInit()
        }
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S){
            binding.settingUseSystemTheme.visibility = View.GONE
        }

        fun isUseSystemColor(systemColor:Boolean){
            if (systemColor){
                binding.settingTheme.visibility = View.GONE
            }else{
                binding.settingTheme.visibility = View.VISIBLE
            }
        }

        binding.systemColor.isChecked = ThemeEngine.getInstance(requireContext()).isDynamicTheme
        isUseSystemColor(binding.systemColor.isChecked)
        binding.settingUseSystemTheme.setOnClickListener {
            ThemeEngine.getInstance(requireContext()).isDynamicTheme = !binding.systemColor.isChecked
            binding.systemColor.isChecked = !binding.systemColor.isChecked
        }
        binding.systemColor.setOnClickListener {
            isUseSystemColor(binding.systemColor.isChecked)
            ThemeEngine.getInstance(requireContext()).isDynamicTheme = binding.systemColor.isChecked
            recreateInit()
        }

    }

    private fun initDebug(){
        AppUtils.getService().get("debug"){
            binding.systemDebug.isChecked = it == "true"
        }

        binding.settingDebug.setOnClickListener {
            binding.systemDebug.isChecked = !binding.systemDebug.isChecked
            AppUtils.getService().set("debug",if(binding.systemDebug.isChecked) "true" else "false")
        }
        binding.systemDebug.setOnClickListener {
            AppUtils.getService().set("debug",if(binding.systemDebug.isChecked) "true" else "false")
        }
       // binding.systemDebug.setOnCheckedChangeListener {_, isChecked -> AppUtils.getService().set("debug",if(isChecked) "true" else "false")}

    }
    /**
     * 页面重新初始化
     */
    fun recreateInit() {
        activity?.recreate()
    }

}