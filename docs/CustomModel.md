ArkPets附加说明文档
# 自定义模型

ArkPets 添加自定义 Spine 模型的方法。

### 前提
- **熟悉 JSON 数据格式。**
- 所要添加的模型的 **Spine版本必须是 3.8** ，这也是截至本文档撰写时《明日方舟》所使用的版本。不同版本的Spine之间的兼容性较差，您可以使用文本编辑器强制查看小人.skel文件的头部信息，以确定其Spine版本。

### 步骤
1. 安装 ArkPets 并确保已在 [选项] 中下载了模型。
2. 将所要添加的模型的资源文件（包括 .atlas .png .skel）放入文件夹 _A_ 中，然后将文件夹 _A_ 放到程序目录中的 `models` 资源文件夹中。
    > 也可以放到其他资源文件夹，但是需要进行额外的操作。假设把文件夹 _A_ 放到了程序目录中的文件夹 _B_ 中，且模型的 `type` 值是 _C_ ，那么程序目录中的 `models_data.json` 的 `storageDirectory` 字段中需要添加键值对 `"C" : "B"`。
3. 在程序目录中的 `models_data.json`，找到 `data` 字段。仿照其他模型对象的格式，加入你所要添加的新模型的信息，示例如下：
   ```json
   "285_medic2": { // 键需要设置为模型文件夹A的名称（重要）
       "assetId": "build_char_285_medic2", // 模型资源文件的纯文件名称（去掉扩展名）
       "type": "Operator",
       "style": null,
       "name": "Lancet-2", // 模型在启动器中显示的名称
       "sortTags": [
           "Operator"
       ],
       "appellation": null,
       "skinGroupId": null,
       "skinGroupName": null,
       "checksum": null
   }
   ```
   上方示例中，大部分字段都可以设为 `null`，它们的具体功能在此不进行介绍，可自行探索。
4. 重新打开启动器即可找到已添加的自定义模型。若未在启动器中找到自定义模型，请检查你的操作和相关拼写。