package com.example.lab10_maxi.data

class SerieRepository(private val api: SerieApiService) {
    suspend fun getAll() = api.selectSeries()
    suspend fun getById(id: Int) = api.selectSerie(id.toString())
    suspend fun insert(serie: SerieModel) = api.insertSerie(serie)
    suspend fun update(id: Int, serie: SerieModel) = api.updateSerie(id.toString(), serie)
    suspend fun delete(id: Int) = api.deleteSerie(id.toString())
}
