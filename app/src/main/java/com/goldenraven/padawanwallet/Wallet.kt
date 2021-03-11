/*
 * Copyright 2020 thunderbiscuit and contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the ./LICENSE file.
 */

package com.goldenraven.padawanwallet

import android.content.Context
import android.content.SharedPreferences
import org.bitcoindevkit.bdkjni.Lib
import org.bitcoindevkit.bdkjni.Types.*
import timber.log.Timber

object Wallet {

    private val lib: Lib
    private lateinit var walletPtr: WalletPtr
    private val name: String = "padawan-testnet-0"
    private lateinit var path: String
    private val electrumURL: String = "tcp://testnet.aranguren.org:51001"

    init {
        // load bitcoindevkit
        Lib.load()
        this.lib = Lib()
    }

    // setting the path requires the application context and is done once by PadawanWalletApplication
    public fun setPath(path: String) {
        this.path = path
    }

    public fun initialize(
        name: String,
        path: String,
        descriptor: String,
        changeDescriptor: String,
        electrumURL: String,
        electrumProxy: String?,
    ): Unit {
        walletPtr = lib.constructor(
            WalletConstructor(
                name = name,
                network = Network.testnet,
                path = path,
                descriptor = descriptor,
                change_descriptor = changeDescriptor,
                electrum_url = electrumURL,
                electrum_proxy = electrumProxy,
            )
        )
    }

    public fun loadExistingWallet(applicationContext: Context): Unit {
        val initialWalletData: RequiredInitialWalletData = Repository.getInitialWalletData(applicationContext)
        Timber.i("[PADAWANLOGS] Descriptor: ${initialWalletData.descriptor}")
        Timber.i("[PADAWANLOGS] Change descriptor: ${initialWalletData.changeDescriptor}")
        this.initialize(
            name = this.name,
            path = this.path,
            descriptor = initialWalletData.descriptor,
            changeDescriptor = initialWalletData.changeDescriptor,
            electrumURL = this.electrumURL,
            electrumProxy = null,
        )
    }

    public fun recoverWallet(editor: SharedPreferences.Editor, mnemonic: String) {
        val keys: ExtendedKeys = createExtendedKeyFromMnemonic(mnemonic)
        val descriptor: String = createDescriptor(keys)
        val changeDescriptor: String = createChangeDescriptor(keys)
        this.initialize(
            name = this.name,
            path = this.path,
            descriptor = descriptor,
            changeDescriptor = changeDescriptor,
            electrumURL = this.electrumURL,
            electrumProxy = null,
        )
        Repository.saveWallet(editor, path, descriptor, changeDescriptor)
        Repository.saveMnemonic(editor, keys.mnemonic)
    }

    public fun createWallet(editor: SharedPreferences.Editor): Unit {
        val keys: ExtendedKeys = generateExtendedKey(12)
        val descriptor: String = createDescriptor(keys)
        val changeDescriptor: String = createChangeDescriptor(keys)
        this.initialize(
            name = this.name,
            path = this.path,
            descriptor = descriptor,
            changeDescriptor = changeDescriptor,
            electrumURL = this.electrumURL,
            electrumProxy = null,
        )
        Repository.saveWallet(editor, path, descriptor, changeDescriptor)
        Repository.saveMnemonic(editor, keys.mnemonic)
    }

    public fun generateExtendedKey(mnemonicWordCount: Int): ExtendedKeys {
        Timber.i("Extended keys generated")
        return lib.generate_extended_key(Network.testnet, mnemonicWordCount)
    }

    public fun createExtendedKeyFromMnemonic(mnemonic: String): ExtendedKeys {
        return lib.create_extended_keys(Network.testnet, mnemonic)
    }

    public fun createDescriptor(keys: ExtendedKeys): String {
        Timber.i("[PADAWANLOGS] Descriptor for receive addresses is wpkh(${keys.ext_priv_key}/84'/1'/0'/0/*)")
        return ("wpkh(" + keys.ext_priv_key + "/84'/1'/0'/0/*)")
    }

    public fun createChangeDescriptor(keys: ExtendedKeys): String {
        Timber.i("[PADAWANLOGS] Descriptor for change addresses is wpkh(${keys.ext_priv_key}/84'/1'/0'/1/*)")
        return ("wpkh(" + keys.ext_priv_key + "/84'/1'/0'/1/*)")
    }

    public fun sync(max_address: Int?=null) {
        lib.sync(walletPtr, max_address)
    }

    public fun getBalance(): Long {
        return lib.get_balance(walletPtr)
    }

    public fun getNewAddress(): String {
        return lib.get_new_address(walletPtr)
    }

    public fun createTransaction(
        fee_rate: Float,
        addressees: List<Pair<String, String>>,
        send_all: Boolean? = false,
        utxos: List<String>? = null,
        unspendable: List<String>? = null,
        policy: Map<String, List<String>>? = null,
    ): CreateTxResponse {
        return lib.create_tx(walletPtr, fee_rate, addressees, send_all, utxos, unspendable, policy)
    }

    public fun sign(psbt: String, assume_height: Int? = null): SignResponse {
        return lib.sign(walletPtr, psbt, assume_height)
    }

    public fun extractPsbt(psbt: String): RawTransaction {
        return lib.extract_psbt(walletPtr, psbt)
    }

    public fun broadcast(raw_tx: String): Txid {
        return lib.broadcast(walletPtr, raw_tx)
    }
}
