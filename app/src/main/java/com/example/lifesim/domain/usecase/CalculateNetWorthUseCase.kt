package com.example.lifesim.domain.usecase

import com.example.lifesim.data.local.dao.AssetDao
import com.example.lifesim.data.local.dao.CharacterDao
import javax.inject.Inject

class CalculateNetWorthUseCase @Inject constructor(
    private val characterDao: CharacterDao,
    private val assetDao: AssetDao
) {
    suspend operator fun invoke(characterId: String): NetWorthResult {
        val character = characterDao.getCharacterById(characterId) ?: return NetWorthResult(0.0, 0.0, 0.0)
        val liquidValue = assetDao.getTotalLiquidValue(characterId) ?: character.cash
        val totalAssets = assetDao.getTotalAssetValue(characterId) ?: 0.0
        val netWorth = character.cash + totalAssets
        return NetWorthResult(netWorth, character.cash, totalAssets)
    }
}

data class NetWorthResult(val totalNetWorth: Double, val liquidCash: Double, val totalAssetsValue: Double)
