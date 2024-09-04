/*
 * Copyright (C) 2024 ankio(ankio@ankio.net)
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

package org.ezbook.server.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ezbook.server.Server
import org.ezbook.server.db.Db
import org.ezbook.server.db.model.BillInfoModel
import org.ezbook.server.db.model.CategoryModel
import org.ezbook.server.db.model.SettingModel

object Category {
    suspend  fun getName(category1:CategoryModel,category2:CategoryModel?): String = withContext(Dispatchers.IO){

      val setting = SettingModel.get("category_show_parent","false")
       val showParent = setting.toBoolean()
        if (category2 === null) {
            return@withContext category1.name!!
        }
        if (showParent) {
            return@withContext "${category1.name!!} - ${category2.name!!}"
        }
        return@withContext category2.name!!
    }

    //只允许在io线程
    fun setCategoryMap(billInfoModel: BillInfoModel) {
        Server.isRunOnMainThread()
        val category = billInfoModel.cateName
        Db.get().categoryMapDao().query(category)?.let {
            billInfoModel.cateName = it.mapName
        }
    }


}