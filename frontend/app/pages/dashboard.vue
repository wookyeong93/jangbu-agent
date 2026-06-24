<script setup lang="ts">
import { Line } from 'vue-chartjs'
import { getDashboard } from '~/api/dashboard'
import { getDailyGuide } from '~/api/guide'
import type { DashboardResponse } from '~/types/dashboard'
import type { GuideResponse } from '~/types/guide'
import type { SelectOption } from '~/types/code'

definePageMeta({ title: '대시보드' })

const MONTHS_OPTIONS: SelectOption[] = [
  { value: '3', label: '최근 3개월' },
  { value: '6', label: '최근 6개월' },
  { value: '12', label: '최근 12개월' }
]

function currentMonthString() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const selectedMonth = ref(currentMonthString())
const selectedMonthsCount = ref('6')
const data = ref<DashboardResponse | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const guide = ref<GuideResponse | null>(null)
const guideLoading = ref(true)
const guideErrorMessage = ref('')

function formatCurrency(value: number) {
  return `${value.toLocaleString('ko-KR')}원`
}

function formatMonth(year: number, month: number) {
  return `${year}.${String(month).padStart(2, '0')}`
}

const chartData = computed(() => {
  if (!data.value) {
    return null
  }
  const trend = data.value.trend
  return {
    labels: trend.map((t) => formatMonth(t.year, t.month)),
    datasets: [
      { label: '매출', data: trend.map((t) => t.totalSale), borderColor: '#2563eb', backgroundColor: '#2563eb' },
      { label: '매입', data: trend.map((t) => t.totalPurchase), borderColor: '#f59e0b', backgroundColor: '#f59e0b' },
      { label: '순익', data: trend.map((t) => t.netProfit), borderColor: '#16a34a', backgroundColor: '#16a34a' }
    ]
  }
})

async function loadDashboard() {
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await getDashboard({
      month: selectedMonth.value,
      months: Number(selectedMonthsCount.value)
    })
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '대시보드를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

async function loadGuide() {
  guideLoading.value = true
  guideErrorMessage.value = ''
  try {
    guide.value = await getDailyGuide()
  } catch (err) {
    guideErrorMessage.value = err instanceof Error ? err.message : '가이드를 불러오지 못했습니다.'
  } finally {
    guideLoading.value = false
  }
}

watch([selectedMonth, selectedMonthsCount], loadDashboard)
onMounted(loadDashboard)
onMounted(loadGuide)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-end justify-end gap-3">
      <div class="space-y-1">
        <label for="dashboard-month" class="text-sm text-[var(--color-text-muted)]">조회월</label>
        <input
          id="dashboard-month"
          v-model="selectedMonth"
          type="month"
          class="block rounded-md border border-[var(--color-border)] bg-white px-3 py-2 text-sm focus:border-[var(--color-primary)] focus:outline-none"
        >
      </div>
      <AppSelectBox v-model="selectedMonthsCount" id="dashboard-months" :options="MONTHS_OPTIONS" class="w-40" />
    </div>

    <section class="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
      <h2 class="mb-2 text-sm font-semibold text-[var(--color-text-muted)]">
        오늘의 AI 가이드
        <span v-if="guide" class="font-normal">· {{ guide.guideDt }}</span>
      </h2>
      <p v-if="guideLoading" class="text-sm text-[var(--color-text-muted)]">가이드를 불러오는 중...</p>
      <p v-else-if="guideErrorMessage" class="text-sm text-red-600">{{ guideErrorMessage }}</p>
      <template v-else-if="guide">
        <p class="whitespace-pre-line text-sm text-[var(--color-text)]">{{ guide.guideText }}</p>
        <p class="mt-3 text-xs text-[var(--color-text-muted)]">
          기준(최근 28일) · 매입 {{ formatCurrency(guide.basedPurchase) }} · 매출 {{ formatCurrency(guide.basedSale) }}
          · 지출 {{ formatCurrency(guide.basedExpense) }} · 순익 {{ formatCurrency(guide.basedProfit) }}
          <template v-if="guide.marginRate !== null"> · 마진율 {{ guide.marginRate.toFixed(1) }}%</template>
        </p>
      </template>
    </section>

    <p v-if="loading" class="text-sm text-[var(--color-text-muted)]">불러오는 중...</p>
    <p v-else-if="errorMessage" class="text-sm text-red-600">{{ errorMessage }}</p>

    <template v-else-if="data">
      <section class="space-y-3">
        <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">
          {{ formatMonth(data.current.year, data.current.month) }} KPI
        </h2>
        <div class="grid grid-cols-2 gap-4 md:grid-cols-5">
          <AppStatCard label="매입" :value="formatCurrency(data.current.totalPurchase)" />
          <AppStatCard label="매출" :value="formatCurrency(data.current.totalSale)" />
          <AppStatCard label="지출" :value="formatCurrency(data.current.totalExpense)" />
          <AppStatCard
            label="순이익"
            :value="formatCurrency(data.current.netProfit)"
            :tone="data.current.netProfit >= 0 ? 'positive' : 'negative'"
          />
          <AppStatCard label="마진율" :value="`${data.current.marginRate.toFixed(1)}%`" />
        </div>
      </section>

      <section class="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
        <h2 class="mb-4 text-sm font-semibold text-[var(--color-text-muted)]">
          {{ formatMonth(data.current.year, data.current.month) }} 기준 최근 추이
        </h2>
        <Line v-if="chartData" :data="chartData" :options="{ responsive: true }" />
        <p v-else class="text-sm text-[var(--color-text-muted)]">표시할 추이 데이터가 없습니다.</p>
      </section>
    </template>
  </div>
</template>
