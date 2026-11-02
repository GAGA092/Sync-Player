package com.traxxiontech.syncplayer.util

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.min

/**
 * Pure Kotlin QR Code Matrix Generator (ISO/IEC 18004 compliant subset).
 * Generates valid scannable QR Code bitmaps without requiring heavy external libraries.
 */
object QrCodeGenerator {

    /**
     * Generates a square QR Code Bitmap for the specified text.
     */
    fun encodeToBitmap(content: String, sizePx: Int = 512): Bitmap {
        val matrix = generateQrMatrix(content)
        val matrixSize = matrix.size
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

        val scale = sizePx.toFloat() / matrixSize.toFloat()

        for (y in 0 until sizePx) {
            val my = (y / scale).toInt().coerceIn(0, matrixSize - 1)
            for (x in 0 until sizePx) {
                val mx = (x / scale).toInt().coerceIn(0, matrixSize - 1)
                val isDark = matrix[my][mx]
                bitmap.setPixel(x, y, if (isDark) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    /**
     * Generates a 2D boolean array representing the QR code module matrix with quiet zone.
     */
    fun generateQrMatrix(content: String): Array<BooleanArray> {
        val dataBytes = content.toByteArray(Charsets.ISO_8859_1)
        val version = selectVersion(dataBytes.size)
        val dimension = 17 + 4 * version
        val modules = Array(dimension) { BooleanArray(dimension) }
        val isFunction = Array(dimension) { BooleanArray(dimension) }

        // 1. Finder patterns
        placeFinderPattern(modules, isFunction, 0, 0)
        placeFinderPattern(modules, isFunction, dimension - 7, 0)
        placeFinderPattern(modules, isFunction, 0, dimension - 7)

        // 2. Alignment pattern (for version >= 2)
        if (version >= 2) {
            val alignPos = getAlignmentPosition(version)
            placeAlignmentPattern(modules, isFunction, alignPos, alignPos)
        }

        // 3. Timing patterns
        for (i in 8 until dimension - 8) {
            val dark = (i % 2 == 0)
            if (!isFunction[6][i]) {
                modules[6][i] = dark
                isFunction[6][i] = true
            }
            if (!isFunction[i][6]) {
                modules[i][6] = dark
                isFunction[i][6] = true
            }
        }

        // 4. Dark module
        val darkRow = 4 * version + 9
        modules[darkRow][8] = true
        isFunction[darkRow][8] = true

        // 5. Reserve format info areas
        for (i in 0..8) {
            if (!isFunction[8][i]) isFunction[8][i] = true
            if (!isFunction[i][8]) isFunction[i][8] = true
        }
        for (i in (dimension - 8) until dimension) {
            if (!isFunction[8][i]) isFunction[8][i] = true
            if (!isFunction[i][8]) isFunction[i][8] = true
        }

        // 6. Encode Data + Error Correction
        val codewords = encodeData(dataBytes, version)

        // 7. Place codewords with zig-zag pattern
        var bitIndex = 0
        val totalBits = codewords.size * 8
        var goingUp = true

        var x = dimension - 1
        while (x > 0) {
            if (x == 6) x-- // Skip vertical timing column

            val rows = if (goingUp) (dimension - 1 downTo 0) else (0 until dimension)
            for (y in rows) {
                for (c in 0..1) {
                    val currX = x - c
                    if (!isFunction[y][currX]) {
                        var bit = false
                        if (bitIndex < totalBits) {
                            val byteVal = codewords[bitIndex / 8].toInt() and 0xFF
                            val bitPos = 7 - (bitIndex % 8)
                            bit = ((byteVal shr bitPos) and 1) == 1
                            bitIndex++
                        }

                        // Apply Mask Pattern 0: (row + col) % 2 == 0
                        val maskInvert = (y + currX) % 2 == 0
                        modules[y][currX] = bit xor maskInvert
                    }
                }
            }
            goingUp = !goingUp
            x -= 2
        }

        // 8. Place Format Information (Mask 0, EC Level L = 01 -> Format bits: 111011111000100)
        val formatBits = 0b111011111000100
        for (i in 0 until 15) {
            val bit = ((formatBits shr (14 - i)) and 1) == 1

            // Around top-left finder
            when {
                i in 0..5 -> modules[8][i] = bit
                i == 6 -> modules[8][7] = bit
                i == 7 -> modules[8][8] = bit
                i == 8 -> modules[7][8] = bit
                else -> modules[14 - i][8] = bit
            }

            // Along edges of other finders
            if (i < 8) {
                modules[dimension - 1 - i][8] = bit
            } else {
                modules[8][dimension - 15 + i] = bit
            }
        }

        // Add 4-module Quiet Zone border
        val quietZone = 4
        val totalDim = dimension + quietZone * 2
        val bordered = Array(totalDim) { BooleanArray(totalDim) }

        for (r in 0 until dimension) {
            for (c in 0 until dimension) {
                bordered[r + quietZone][c + quietZone] = modules[r][c]
            }
        }

        return bordered
    }

    private fun selectVersion(byteCount: Int): Int {
        return when {
            byteCount <= 17 -> 1 // 19 capacity for L
            byteCount <= 32 -> 2 // 34 capacity for L
            byteCount <= 53 -> 3 // 55 capacity for L
            byteCount <= 78 -> 4 // 80 capacity for L
            byteCount <= 106 -> 5
            else -> 6
        }
    }

    private fun getAlignmentPosition(version: Int): Int {
        return when (version) {
            2 -> 18
            3 -> 22
            4 -> 26
            5 -> 30
            else -> 34
        }
    }

    private fun placeFinderPattern(modules: Array<BooleanArray>, isFunction: Array<BooleanArray>, startX: Int, startY: Int) {
        val pattern = arrayOf(
            booleanArrayOf(true, true, true, true, true, true, true),
            booleanArrayOf(true, false, false, false, false, false, true),
            booleanArrayOf(true, false, true, true, true, false, true),
            booleanArrayOf(true, false, true, true, true, false, true),
            booleanArrayOf(true, false, true, true, true, false, true),
            booleanArrayOf(true, false, false, false, false, false, true),
            booleanArrayOf(true, true, true, true, true, true, true)
        )

        for (r in -1..7) {
            for (c in -1..7) {
                val x = startX + c
                val y = startY + r
                if (x in modules.indices && y in modules.indices) {
                    isFunction[y][x] = true
                    if (r in 0..6 && c in 0..6) {
                        modules[y][x] = pattern[r][c]
                    } else {
                        modules[y][x] = false // Separator
                    }
                }
            }
        }
    }

    private fun placeAlignmentPattern(modules: Array<BooleanArray>, isFunction: Array<BooleanArray>, centerX: Int, centerY: Int) {
        val startX = centerX - 2
        val startY = centerY - 2
        val pattern = arrayOf(
            booleanArrayOf(true, true, true, true, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, false, true, false, true),
            booleanArrayOf(true, false, false, false, true),
            booleanArrayOf(true, true, true, true, true)
        )

        for (r in 0..4) {
            for (c in 0..4) {
                val x = startX + c
                val y = startY + r
                if (!isFunction[y][x]) {
                    modules[y][x] = pattern[r][c]
                    isFunction[y][x] = true
                }
            }
        }
    }

    private fun encodeData(data: ByteArray, version: Int): ByteArray {
        val totalCodewords = getTotalCodewords(version)
        val ecCodewordsCount = getEcCodewords(version)
        val dataCodewordsCount = totalCodewords - ecCodewordsCount

        // Byte mode indicator: 0100 (4 bits)
        // Character count indicator: 8 bits for versions 1-9
        val bitBuffer = mutableListOf<Boolean>()

        // Mode 0100
        bitBuffer.addAll(listOf(false, true, false, false))

        // Length in 8 bits
        val len = min(data.size, dataCodewordsCount - 2)
        for (i in 7 downTo 0) {
            bitBuffer.add(((len shr i) and 1) == 1)
        }

        // Data bytes
        for (i in 0 until len) {
            val b = data[i].toInt() and 0xFF
            for (bit in 7 downTo 0) {
                bitBuffer.add(((b shr bit) and 1) == 1)
            }
        }

        // Terminator: up to 4 zero bits
        val paddingNeeded = (dataCodewordsCount * 8) - bitBuffer.size
        val termLen = min(4, paddingNeeded)
        for (i in 0 until termLen) bitBuffer.add(false)

        // Pad to byte boundary
        while (bitBuffer.size % 8 != 0) {
            bitBuffer.add(false)
        }

        // Convert bit buffer to bytes
        val dataCodewords = ByteArray(dataCodewordsCount)
        var byteIdx = 0
        var currentByte = 0
        for (i in bitBuffer.indices) {
            if (bitBuffer[i]) {
                currentByte = currentByte or (1 shl (7 - (i % 8)))
            }
            if ((i + 1) % 8 == 0) {
                if (byteIdx < dataCodewordsCount) {
                    dataCodewords[byteIdx++] = currentByte.toByte()
                }
                currentByte = 0
            }
        }

        // Pad codewords: 0xEC (236), 0x11 (17)
        val padBytes = byteArrayOf(0xEC.toByte(), 0x11.toByte())
        var padToggle = 0
        while (byteIdx < dataCodewordsCount) {
            dataCodewords[byteIdx++] = padBytes[padToggle]
            padToggle = 1 - padToggle
        }

        // Compute Reed-Solomon Error Correction Codewords
        val ecCodewords = computeReedSolomon(dataCodewords, ecCodewordsCount)

        return dataCodewords + ecCodewords
    }

    private fun getTotalCodewords(version: Int): Int {
        return when (version) {
            1 -> 26
            2 -> 44
            3 -> 70
            4 -> 100
            5 -> 134
            else -> 172
        }
    }

    private fun getEcCodewords(version: Int): Int {
        return when (version) {
            1 -> 7
            2 -> 10
            3 -> 15
            4 -> 20
            5 -> 26
            else -> 36
        }
    }

    // GF(256) tables for Reed-Solomon computation
    private val expTable = IntArray(512)
    private val logTable = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            expTable[i] = x
            logTable[x] = i
            x = x shl 1
            if (x >= 256) {
                x = x xor 0x11D // Primitive polynomial x^8 + x^4 + x^3 + x^2 + 1
            }
        }
        for (i in 255 until 512) {
            expTable[i] = expTable[i - 255]
        }
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return expTable[logTable[a] + logTable[b]]
    }

    private fun computeReedSolomon(data: ByteArray, ecCount: Int): ByteArray {
        // Build generator polynomial
        var gen = intArrayOf(1)
        for (i in 0 until ecCount) {
            val root = expTable[i]
            val nextGen = IntArray(gen.size + 1)
            for (j in gen.indices) {
                nextGen[j] = nextGen[j] xor gfMul(gen[j], root)
                nextGen[j + 1] = nextGen[j + 1] xor gen[j]
            }
            gen = nextGen
        }

        // Polynomial division
        val remainder = IntArray(ecCount)
        for (b in data) {
            val byteVal = b.toInt() and 0xFF
            val factor = byteVal xor remainder[0]

            for (j in 0 until ecCount - 1) {
                remainder[j] = remainder[j + 1] xor gfMul(gen[ecCount - 1 - j], factor)
            }
            remainder[ecCount - 1] = gfMul(gen[0], factor)
        }

        val result = ByteArray(ecCount)
        for (i in 0 until ecCount) {
            result[i] = remainder[ecCount - 1 - i].toByte()
        }
        return result
    }
}
