package tiOPF.Mapper

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import tiOPF.Log.LOG
import java.io.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

fun Project.writeToXML(file: File){
    val mapperXML = MapperXML(this)
    mapperXML.write(FileOutputStream(file))
}

fun Project.readFromXML(file: File){
    val mapperXML = MapperXML(this)
    mapperXML.read(FileInputStream(file))
}

class MapperXML(private val project: Project) {
    private fun Node.forEachElement(iter: (Element) -> Unit){
        var child = firstChild
        while (child != null) {
            if (child is Element)
                iter(child)
            child = child.nextSibling
        }
    }

    private fun Node.forEach(iter: (Node) -> Unit){
        var child = firstChild
        while (child != null) {
            iter(child)
            child = child.nextSibling
        }
    }

    private fun NamedNodeMap.forEach(iter: (Node) -> Unit){
        for (i in 0 until this.length)
            iter(item(i))
    }

    fun read(inputStream: InputStream){
        val builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)
        val projectElement = doc.documentElement
        domToObject(projectElement, project)
    }

    private fun hyphenToName(name: String): String{
        var result = name
        val indexes = mutableListOf<Int>()
        for (i in name.indices){
            if (name[i] == '-')
                indexes.add(0, i) // add in reverse
        }

        indexes.forEach {i ->
            result = result.substring(0 until i) + result[i+1].toUpperCase()+  result.substring(i+2 until result.length)

        }
        return result
    }

    private fun nameToHyphen(name: String): String{
        var nameLower = name.toLowerCase()
        val indexes = mutableListOf<Int>()
        for (i in name.indices)
            if (name[i] != nameLower[i])
                indexes.add(0, i) // add in reverse
        indexes.forEach {
            nameLower = nameLower.substring(0 until it) + "-" + nameLower.substring(it, nameLower.length)
        }

        return nameLower

    }

    private fun domToObject(element: Element, o: Any?){
        if (o == null)
            return
        val props = o::class.memberProperties
        // attributes first
        element.attributes.forEach {
            val name = hyphenToName(it.nodeName)
            val prop = props.find { prop ->
                val item = prop.findAnnotation<Item>()
                prop.name == name ||  (item != null && item.name == it.nodeName)
            }
            if (prop != null && prop is KMutableProperty<*>) {
                when (val propClass = prop.getter.returnType.classifier as KClass<*>) {
                    String::class -> prop.setter.call(o, it.nodeValue)
                    Int::class -> prop.setter.call(o, it.nodeValue.toInt())
                    Float::class -> prop.setter.call(o, it.nodeValue.toFloat())
                    Boolean::class -> prop.setter.call(o, it.nodeValue!!.toBoolean())
                    Project.EnumType::class -> prop.setter.call(o, Project.EnumType.valueOf(it.nodeValue.capitalize()))
                    else -> LOG("unhandled attr :${propClass.simpleName}")
                }
            }
            else {
                LOG("unable to find prop for ${it.nodeName}")
            }
        }
        // now children
        element.forEachElement {
            val name = hyphenToName(it.nodeName)
            val prop = props.find { prop -> prop.name == name }
            if (prop != null) {
                val objProp = prop.getter.call(o)
                if (objProp != null) {
                    when (objProp) {
                        is MutableList<*> -> {
                            val itemClass = prop.findAnnotation<ItemClass>()
                            if (itemClass != null) {
                                it.forEachElement { child ->
                                    if(itemClass.kClass == String::class){
                                        @Suppress("UNCHECKED_CAST")
                                        (objProp as MutableList<String>).add(child.textContent)
                                    }
                                    else {
                                        val item = (
                                                if (itemClass.kClass == Project.Unit.ClassItem.Selection::class)
                                                    Project.Unit.ClassItem.SelectionFunction()
                                                else
                                                    itemClass.kClass.createInstance()
                                                )
                                        @Suppress("UNCHECKED_CAST")
                                        (objProp as MutableList<Any>).add(item)
                                        domToObject(child, item)
                                    }
                                }
                            }
                            else
                                LOG("couldn't find item class for $name ${prop.annotations}")

                        }
                        is Project.Unit.ClassItem.Mapping -> {
                            domToObject(it, objProp)
                        }
                        else ->  LOG("unhandled type: ${objProp::class.simpleName}")
                    }
                }
                else {
                    val propType = prop.returnType.classifier as KClass<*>
                    if (prop is KMutableProperty<*> && propType == String::class) {
                        if (prop.findAnnotation<CDATA>() != null){
                            it.forEach {cdata ->
                                if (cdata.nodeName == "#cdata-section")
                                    prop.setter.call(o, cdata.nodeValue)
                            }
                        }
                        else
                            prop.setter.call(o, it.textContent)
                    }
                }
            }
            else {
                if (o is Project.Unit.ClassItem.Mapping) {
                    val m = Project.Unit.ClassItem.Mapping.PropMap()
                    o.mappings.add(m)
                    domToObject(it, m)
                }
                else
                    LOG("${it.nodeName} has no matching prop")
            }
        }
    }

    private fun objectToDOM(doc: Document, parent: Element, o: Any?){
        if (o == null)
            return
        val props = o::class.memberProperties
        props.forEach {
            if (it.visibility == KVisibility.PUBLIC) {
                val value = it.getter.call(o)
                if (value != null) {
                    val nodeName = nameToHyphen(it.name)
                    when (value) {
                        is List<*> -> {
                            val node = (if (it.findAnnotation<NoParent>() == null) doc.createElement(nodeName) else parent )
                            if (parent != node) {
                                val comment = it.findAnnotation<Comment>()
                                if (comment != null ) {
                                    val el = doc.createComment(" ${comment.value} ")
                                    parent.appendChild(el)
                                }
                                parent.appendChild(node)
                            }
                            val itemAnnotation = it.findAnnotation<Item>()
                            val itemName = (itemAnnotation?.name ?: "item")
                            value.forEach { child ->
                                val newItem = doc.createElement(itemName)
                                node.appendChild(newItem)
                                objectToDOM(doc, newItem, child)
                            }
                        }
                        is String -> {
                            if (it.findAnnotation<CDATA>() != null){
                                val node = doc.createElement(nodeName)
                                parent.appendChild(node)
                                val cdata = doc.createCDATASection(value.trimIndent())
                                node.appendChild(cdata)
                            }
                            else
                                parent.setAttribute(nodeName, value)
                        }
                        is Int, is Boolean, is Float -> parent.setAttribute(nodeName, value.toString())
                        is Project.EnumType -> parent.setAttribute(nodeName, value.toString().toLowerCase())
                        is Project.Unit.ClassItem.Selection.SelectionType -> parent.setAttribute(nodeName, value.toString().toLowerCase())
                        is Project.Unit.ClassItem.Mapping -> {
                            val node = doc.createElement(nodeName)
                            parent.appendChild(node)
                            objectToDOM(doc, node, value)
                        }
                        else -> LOG("unsupported type ${value::class.simpleName}")
                    }
                }
            }
        }
    }

    fun write(outputStream: OutputStream){
        val builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val doc = docBuilder.newDocument()
        val root = doc.createElement("project")
        doc.appendChild(root)
        objectToDOM(doc, root, project)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val source = DOMSource(doc)

        val result = StreamResult(StringWriter())

        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "5")
        transformer.transform(source, result)
        outputStream.write(result.writer.toString().toByteArray())
    }

}
