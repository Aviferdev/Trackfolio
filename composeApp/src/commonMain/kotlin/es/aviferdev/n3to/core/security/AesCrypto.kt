package es.aviferdev.n3to.core.security

import kotlin.experimental.xor

/**
 * AES-256-CBC + PKCS7 implementado en pure Kotlin (sin dependencias nativas).
 * PBKDF2-HMAC-SHA256 para derivación de clave.
 *
 * Usado en commonMain para que la lógica de cifrado sea idéntica en Android e iOS.
 * Solo el I/O del fichero y el Share Sheet son expect/actual.
 */
object AesCrypto {

    // ── AES S-Box y constantes ─────────────────────────────────────────────────
    private val SBOX = intArrayOf(
        0x63,0x7c,0x77,0x7b,0xf2,0x6b,0x6f,0xc5,0x30,0x01,0x67,0x2b,0xfe,0xd7,0xab,0x76,
        0xca,0x82,0xc9,0x7d,0xfa,0x59,0x47,0xf0,0xad,0xd4,0xa2,0xaf,0x9c,0xa4,0x72,0xc0,
        0xb7,0xfd,0x93,0x26,0x36,0x3f,0xf7,0xcc,0x34,0xa5,0xe5,0xf1,0x71,0xd8,0x31,0x15,
        0x04,0xc7,0x23,0xc3,0x18,0x96,0x05,0x9a,0x07,0x12,0x80,0xe2,0xeb,0x27,0xb2,0x75,
        0x09,0x83,0x2c,0x1a,0x1b,0x6e,0x5a,0xa0,0x52,0x3b,0xd6,0xb3,0x29,0xe3,0x2f,0x84,
        0x53,0xd1,0x00,0xed,0x20,0xfc,0xb1,0x5b,0x6a,0xcb,0xbe,0x39,0x4a,0x4c,0x58,0xcf,
        0xd0,0xef,0xaa,0xfb,0x43,0x4d,0x33,0x85,0x45,0xf9,0x02,0x7f,0x50,0x3c,0x9f,0xa8,
        0x51,0xa3,0x40,0x8f,0x92,0x9d,0x38,0xf5,0xbc,0xb6,0xda,0x21,0x10,0xff,0xf3,0xd2,
        0xcd,0x0c,0x13,0xec,0x5f,0x97,0x44,0x17,0xc4,0xa7,0x7e,0x3d,0x64,0x5d,0x19,0x73,
        0x60,0x81,0x4f,0xdc,0x22,0x2a,0x90,0x88,0x46,0xee,0xb8,0x14,0xde,0x5e,0x0b,0xdb,
        0xe0,0x32,0x3a,0x0a,0x49,0x06,0x24,0x5c,0xc2,0xd3,0xac,0x62,0x91,0x95,0xe4,0x79,
        0xe7,0xc8,0x37,0x6d,0x8d,0xd5,0x4e,0xa9,0x6c,0x56,0xf4,0xea,0x65,0x7a,0xae,0x08,
        0xba,0x78,0x25,0x2e,0x1c,0xa6,0xb4,0xc6,0xe8,0xdd,0x74,0x1f,0x4b,0xbd,0x8b,0x8a,
        0x70,0x3e,0xb5,0x66,0x48,0x03,0xf6,0x0e,0x61,0x35,0x57,0xb9,0x86,0xc1,0x1d,0x9e,
        0xe1,0xf8,0x98,0x11,0x69,0xd9,0x8e,0x94,0x9b,0x1e,0x87,0xe9,0xce,0x55,0x28,0xdf,
        0x8c,0xa1,0x89,0x0d,0xbf,0xe6,0x42,0x68,0x41,0x99,0x2d,0x0f,0xb0,0x54,0xbb,0x16
    )

    private val INV_SBOX = IntArray(256).also { inv ->
        SBOX.forEachIndexed { i, v -> inv[v] = i }
    }

    private val RCON = intArrayOf(
        0x01,0x02,0x04,0x08,0x10,0x20,0x40,0x80,0x1b,0x36
    )

    // ── PBKDF2-HMAC-SHA256 (pure Kotlin) ──────────────────────────────────────
    fun pbkdf2(password: ByteArray, salt: ByteArray, iterations: Int, keyLen: Int): ByteArray {
        val result   = ByteArray(keyLen)
        var offset   = 0
        var blockNum = 1
        while (offset < keyLen) {
            val block = pbkdf2Block(password, salt, iterations, blockNum++)
            val copy  = minOf(block.size, keyLen - offset)
            block.copyInto(result, offset, 0, copy)
            offset += copy
        }
        return result
    }

    private fun pbkdf2Block(password: ByteArray, salt: ByteArray, iterations: Int, blockNum: Int): ByteArray {
        val saltWithBlock = salt + intToBytes(blockNum)
        var u = hmacSha256(password, saltWithBlock)
        val result = u.copyOf()
        repeat(iterations - 1) {
            u = hmacSha256(password, u)
            for (i in result.indices) result[i] = result[i] xor u[i]
        }
        return result
    }

    // ── HMAC-SHA256 ────────────────────────────────────────────────────────────
    fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val blockSize   = 64
        val effectiveKey = if (key.size > blockSize) sha256(key) else key
        val paddedKey   = effectiveKey.copyOf(blockSize)
        val ipad        = ByteArray(blockSize) { (paddedKey[it] xor 0x36.toByte()) }
        val opad        = ByteArray(blockSize) { (paddedKey[it] xor 0x5c.toByte()) }
        val inner       = sha256(ipad + data)
        return sha256(opad + inner)
    }

    // ── SHA-256 ────────────────────────────────────────────────────────────────
    fun sha256(data: ByteArray): ByteArray {
        val k = intArrayOf(
            0x428a2f98,0x71374491,0xb5c0fbcf.toInt(),0xe9b5dba5.toInt(),
            0x3956c25b,0x59f111f1,0x923f82a4.toInt(),0xab1c5ed5.toInt(),
            0xd807aa98.toInt(),0x12835b01,0x243185be,0x550c7dc3,
            0x72be5d74,0x80deb1fe.toInt(),0x9bdc06a7.toInt(),0xc19bf174.toInt(),
            0xe49b69c1.toInt(),0xefbe4786.toInt(),0x0fc19dc6,0x240ca1cc,
            0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
            0x983e5152.toInt(),0xa831c66d.toInt(),0xb00327c8.toInt(),0xbf597fc7.toInt(),
            0xc6e00bf3.toInt(),0xd5a79147.toInt(),0x06ca6351,0x14292967,
            0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,
            0x650a7354,0x766a0abb,0x81c2c92e.toInt(),0x92722c85.toInt(),
            0xa2bfe8a1.toInt(),0xa81a664b.toInt(),0xc24b8b70.toInt(),0xc76c51a3.toInt(),
            0xd192e819.toInt(),0xd6990624.toInt(),0xf40e3585.toInt(),0x106aa070,
            0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,
            0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
            0x748f82ee,0x78a5636f,0x84c87814.toInt(),0x8cc70208.toInt(),
            0x90befffa.toInt(),0xa4506ceb.toInt(),0xbef9a3f7.toInt(),0xc67178f2.toInt()
        )
        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        val msgLen    = data.size
        val bitLen    = msgLen.toLong() * 8
        val padded    = run {
            val padLen = if ((msgLen + 1) % 64 <= 56) 56 - (msgLen + 1) % 64
                         else 64 + 56 - (msgLen + 1) % 64
            val out    = ByteArray(msgLen + 1 + padLen + 8)
            data.copyInto(out)
            out[msgLen] = 0x80.toByte()
            for (i in 0..7) out[out.size - 8 + i] = ((bitLen shr ((7 - i) * 8)) and 0xFF).toByte()
            out
        }

        for (chunkStart in padded.indices step 64) {
            val w = IntArray(64)
            for (i in 0..15) {
                w[i] = ((padded[chunkStart + i * 4].toInt() and 0xFF) shl 24) or
                       ((padded[chunkStart + i * 4 + 1].toInt() and 0xFF) shl 16) or
                       ((padded[chunkStart + i * 4 + 2].toInt() and 0xFF) shl 8) or
                        (padded[chunkStart + i * 4 + 3].toInt() and 0xFF)
            }
            for (i in 16..63) {
                val s0 = w[i-15].rotateRight(7) xor w[i-15].rotateRight(18) xor (w[i-15] ushr 3)
                val s1 = w[i-2].rotateRight(17) xor w[i-2].rotateRight(19)  xor (w[i-2] ushr 10)
                w[i] = w[i-16] + s0 + w[i-7] + s1
            }
            var a = h0; var b = h1; var c = h2; var d = h3
            var e = h4; var f = h5; var g = h6; var h = h7
            for (i in 0..63) {
                val S1   = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
                val ch   = (e and f) xor (e.inv() and g)
                val temp1= h + S1 + ch + k[i] + w[i]
                val S0   = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
                val maj  = (a and b) xor (a and c) xor (b and c)
                val temp2= S0 + maj
                h = g; g = f; f = e; e = d + temp1
                d = c; c = b; b = a; a = temp1 + temp2
            }
            h0 += a; h1 += b; h2 += c; h3 += d
            h4 += e; h5 += f; h6 += g; h7 += h
        }

        val digest = ByteArray(32)
        for (i in 0..3) {
            digest[i]      = ((h0 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[4+i]    = ((h1 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[8+i]    = ((h2 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[12+i]   = ((h3 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[16+i]   = ((h4 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[20+i]   = ((h5 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[24+i]   = ((h6 ushr ((3-i)*8)) and 0xFF).toByte()
            digest[28+i]   = ((h7 ushr ((3-i)*8)) and 0xFF).toByte()
        }
        return digest
    }

    // ── AES-256-CBC encrypt ────────────────────────────────────────────────────
    fun encryptCbc(plaintext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes" }
        require(iv.size  == 16) { "IV must be 16 bytes" }
        val padded     = pkcs7Pad(plaintext)
        val expandedKey = expandKey(key)
        val result     = ByteArray(padded.size)
        var prev       = iv.copyOf()
        for (block in 0 until padded.size / 16) {
            val input = ByteArray(16) { padded[block * 16 + it] xor prev[it] }
            val enc   = aesEncryptBlock(input, expandedKey)
            enc.copyInto(result, block * 16)
            prev = enc
        }
        return result
    }

    // ── AES-256-CBC decrypt ────────────────────────────────────────────────────
    fun decryptCbc(ciphertext: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 32)
        require(iv.size  == 16)
        require(ciphertext.size % 16 == 0)
        val expandedKey = expandKeyInv(key)
        val result      = ByteArray(ciphertext.size)
        var prev        = iv.copyOf()
        for (block in 0 until ciphertext.size / 16) {
            val input = ciphertext.sliceArray(block * 16 until (block + 1) * 16)
            val dec   = aesDecryptBlock(input, expandedKey)
            for (i in 0..15) result[block * 16 + i] = (dec[i] xor prev[i])
            prev = input
        }
        return pkcs7Unpad(result)
    }

    // ── AES internals ─────────────────────────────────────────────────────────
    private fun expandKey(key: ByteArray): Array<IntArray> {
        val nk = 8; val nr = 14; val nb = 4
        val w = Array(nb * (nr + 1)) { IntArray(4) }
        for (i in 0 until nk) {
            w[i] = intArrayOf(
                key[i*4].toInt() and 0xFF, key[i*4+1].toInt() and 0xFF,
                key[i*4+2].toInt() and 0xFF, key[i*4+3].toInt() and 0xFF
            )
        }
        for (i in nk until nb * (nr + 1)) {
            var temp = w[i - 1].copyOf()
            if (i % nk == 0) {
                temp = subWord(rotWord(temp))
                temp[0] = temp[0] xor RCON[i / nk - 1]
            } else if (nk > 6 && i % nk == 4) {
                temp = subWord(temp)
            }
            w[i] = IntArray(4) { w[i - nk][it] xor temp[it] }
        }
        return w
    }

    private fun expandKeyInv(key: ByteArray) = expandKey(key)

    private fun subWord(w: IntArray)  = IntArray(4) { SBOX[w[it]] }
    private fun rotWord(w: IntArray)  = intArrayOf(w[1], w[2], w[3], w[0])

    private fun aesEncryptBlock(block: ByteArray, w: Array<IntArray>): ByteArray {
        val state = Array(4) { r -> IntArray(4) { c -> block[r + 4 * c].toInt() and 0xFF } }
        addRoundKey(state, w, 0)
        for (round in 1..13) {
            subBytes(state); shiftRows(state); mixColumns(state); addRoundKey(state, w, round)
        }
        subBytes(state); shiftRows(state); addRoundKey(state, w, 14)
        return ByteArray(16) { state[it % 4][it / 4].toByte() }
    }

    private fun aesDecryptBlock(block: ByteArray, w: Array<IntArray>): ByteArray {
        val state = Array(4) { r -> IntArray(4) { c -> block[r + 4 * c].toInt() and 0xFF } }
        addRoundKey(state, w, 14)
        for (round in 13 downTo 1) {
            invShiftRows(state); invSubBytes(state); addRoundKey(state, w, round); invMixColumns(state)
        }
        invShiftRows(state); invSubBytes(state); addRoundKey(state, w, 0)
        return ByteArray(16) { state[it % 4][it / 4].toByte() }
    }

    private fun addRoundKey(state: Array<IntArray>, w: Array<IntArray>, round: Int) {
        for (c in 0..3) for (r in 0..3) state[r][c] = state[r][c] xor w[round * 4 + c][r]
    }

    private fun subBytes(state: Array<IntArray>)    { for (r in 0..3) for (c in 0..3) state[r][c] = SBOX[state[r][c]] }
    private fun invSubBytes(state: Array<IntArray>)  { for (r in 0..3) for (c in 0..3) state[r][c] = INV_SBOX[state[r][c]] }

    private fun shiftRows(state: Array<IntArray>) {
        for (r in 1..3) { val row = IntArray(4) { state[r][(it + r) % 4] }; row.copyInto(state[r]) }
    }
    private fun invShiftRows(state: Array<IntArray>) {
        for (r in 1..3) { val row = IntArray(4) { state[r][(it - r + 4) % 4] }; row.copyInto(state[r]) }
    }

    private fun mixColumns(state: Array<IntArray>) {
        for (c in 0..3) {
            val s = state.map { it[c] }
            state[0][c] = gm2(s[0]) xor gm3(s[1]) xor s[2] xor s[3]
            state[1][c] = s[0] xor gm2(s[1]) xor gm3(s[2]) xor s[3]
            state[2][c] = s[0] xor s[1] xor gm2(s[2]) xor gm3(s[3])
            state[3][c] = gm3(s[0]) xor s[1] xor s[2] xor gm2(s[3])
        }
    }
    private fun invMixColumns(state: Array<IntArray>) {
        for (c in 0..3) {
            val s = state.map { it[c] }
            state[0][c] = gm(s[0],14) xor gm(s[1],11) xor gm(s[2],13) xor gm(s[3],9)
            state[1][c] = gm(s[0],9)  xor gm(s[1],14) xor gm(s[2],11) xor gm(s[3],13)
            state[2][c] = gm(s[0],13) xor gm(s[1],9)  xor gm(s[2],14) xor gm(s[3],11)
            state[3][c] = gm(s[0],11) xor gm(s[1],13) xor gm(s[2],9)  xor gm(s[3],14)
        }
    }

    private fun gm2(b: Int): Int = if (b and 0x80 != 0) (b shl 1) xor 0x1b and 0xFF else (b shl 1) and 0xFF
    private fun gm3(b: Int): Int = gm2(b) xor b
    private fun gm(b: Int, n: Int): Int {
        var r = b; var m = n; var acc = 0
        while (m > 0) { if (m and 1 != 0) acc = acc xor r; r = gm2(r); m = m shr 1 }
        return acc
    }

    private fun pkcs7Pad(data: ByteArray): ByteArray {
        val pad = 16 - (data.size % 16)
        return data + ByteArray(pad) { pad.toByte() }
    }

    private fun pkcs7Unpad(data: ByteArray): ByteArray {
        val pad = data.last().toInt() and 0xFF
        return data.copyOf(data.size - pad)
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    fun intToBytes(v: Int): ByteArray = byteArrayOf(
        (v shr 24).toByte(), (v shr 16).toByte(), (v shr 8).toByte(), v.toByte()
    )

    fun bytesToInt(b: ByteArray): Int =
        (b[0].toInt() and 0xFF shl 24) or (b[1].toInt() and 0xFF shl 16) or
        (b[2].toInt() and 0xFF shl 8)  or (b[3].toInt() and 0xFF)

    private fun Int.rotateRight(n: Int) = (this ushr n) or (this shl (32 - n))
}
