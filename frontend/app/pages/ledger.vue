<script setup lang="ts">
import { createLedger, deleteLedger, getMonthlyLedger, updateLedger } from '~/api/ledger'
import type { LedgerItem, LedgerMonthlyResponse } from '~/types/ledger'
import type { SelectOption } from '~/types/code'

const modal = useModalStore()

definePageMeta({ title: '장부' })

const TRX_TYPE_OPTIONS: SelectOption[] = [
  { value: 'PURCHASE', label: '매입' },
  { value: 'SALE', label: '매출' },
  { value: 'EXPENSE', label: '지출' }
]

const TRX_TYPE_LABEL: Record<string, string> = {
  PURCHASE: '매입',
  SALE: '매출',
  EXPENSE: '지출'
}

function currentMonthString() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function todayString() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

interface LedgerForm {
  trxType: string
  trxDate: string
  trxName: string
}

function emptyForm(): LedgerForm {
  return {
    trxType: 'PURCHASE',
    trxDate: todayString(),
    trxName: ''
  }
}

const selectedMonth = ref(currentMonthString())
const data = ref<LedgerMonthlyResponse | null>(null)
const loading = ref(true)
const errorMessage = ref('')

const isModalOpen = ref(false)
const editingLedgerNo = ref<number | null>(null)
const form = ref<LedgerForm>(emptyForm())
const amountInput = ref('0')
const submitting = ref(false)
const formError = ref('')

function formatCurrency(value: number) {
  return `${value.toLocaleString('ko-KR')}원`
}

function formatMonth(year: number, month: number) {
  return `${year}.${String(month).padStart(2, '0')}`
}

async function loadLedger() {
  const [yearStr, monthStr] = selectedMonth.value.split('-')
  loading.value = true
  errorMessage.value = ''
  try {
    data.value = await getMonthlyLedger(Number(yearStr), Number(monthStr))
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '장부를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function openModal(item?: LedgerItem) {
  if (item) {
    editingLedgerNo.value = item.ledgerNo
    form.value = {
      trxType: item.trxType,
      trxDate: item.trxDate,
      trxName: item.trxName ?? ''
    }
    amountInput.value = String(item.amount)
  } else {
    editingLedgerNo.value = null
    form.value = emptyForm()
    amountInput.value = '0'
  }
  formError.value = ''
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
}

async function submitForm() {
  formError.value = ''
  const amount = Number(amountInput.value)
  if (!Number.isInteger(amount) || amount < 0) {
    formError.value = '금액은 0 이상의 정수로 입력해주세요.'
    return
  }
  if (form.value.trxDate > todayString()) {
    formError.value = '거래일은 오늘 이전 날짜만 선택할 수 있습니다.'
    return
  }

  submitting.value = true
  try {
    if (editingLedgerNo.value) {
      await updateLedger(editingLedgerNo.value, {
        trxType: form.value.trxType,
        trxDate: form.value.trxDate,
        amount,
        trxName: form.value.trxName || undefined
      })
    } else {
      await createLedger({
        trxType: form.value.trxType,
        amount,
        trxDate: form.value.trxDate,
        trxName: form.value.trxName || undefined
      })
    }
    closeModal()
    await loadLedger()
  } catch (err) {
    formError.value = err instanceof Error ? err.message : '저장에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item: LedgerItem) {
  const ok = await modal.confirm({
    title: '삭제 확인',
    message: `${item.trxDate} ${TRX_TYPE_LABEL[item.trxType] ?? item.trxType} 내역을 삭제할까요?`,
    variant: 'danger',
    confirmText: '삭제'
  })
  if (!ok) {
    return
  }

  try {
    await deleteLedger(item.ledgerNo)
    await loadLedger()
  } catch (err) {
    errorMessage.value = err instanceof Error ? err.message : '삭제에 실패했습니다.'
  }
}

watch(selectedMonth, loadLedger)
onMounted(loadLedger)
</script>

<template>
  <div class="space-y-6">
    <div class="flex flex-wrap items-end justify-between gap-3">
      <div class="space-y-1">
        <label for="ledger-month" class="text-sm text-[var(--color-text-muted)]">조회월</label>
        <input
          id="ledger-month"
          v-model="selectedMonth"
          type="month"
          class="block rounded-md border border-[var(--color-border)] bg-white px-3 py-2 text-sm focus:border-[var(--color-primary)] focus:outline-none"
        >
      </div>
      <AppButton @click="openModal()">+ 등록</AppButton>
    </div>

    <p v-if="loading" class="text-sm text-[var(--color-text-muted)]">불러오는 중...</p>
    <p v-else-if="errorMessage" class="text-sm text-red-600">{{ errorMessage }}</p>

    <template v-else-if="data">
      <section class="space-y-3">
        <h2 class="text-sm font-semibold text-[var(--color-text-muted)]">
          {{ formatMonth(data.year, data.month) }} KPI
        </h2>
        <div class="grid grid-cols-2 gap-4 md:grid-cols-5">
          <AppStatCard label="매입" :value="formatCurrency(data.totalPurchase)" />
          <AppStatCard label="매출" :value="formatCurrency(data.totalSale)" />
          <AppStatCard label="지출" :value="formatCurrency(data.totalExpense)" />
          <AppStatCard
            label="순이익"
            :value="formatCurrency(data.netProfit)"
            :tone="data.netProfit >= 0 ? 'positive' : 'negative'"
          />
          <AppStatCard label="마진율" :value="`${data.marginRate.toFixed(1)}%`" />
        </div>
      </section>

      <section class="rounded-lg border border-[var(--color-border)] bg-[var(--color-surface)] p-4">
        <h2 class="mb-4 text-sm font-semibold text-[var(--color-text-muted)]">
          {{ formatMonth(data.year, data.month) }} 거래 내역
        </h2>
        <p v-if="data.items.length === 0" class="text-sm text-[var(--color-text-muted)]">
          등록된 내역이 없습니다.
        </p>
        <table v-else class="w-full text-sm">
          <thead>
            <tr class="border-b border-[var(--color-border)] text-left text-[var(--color-text-muted)]">
              <th class="py-2 pr-4 font-medium">거래일</th>
              <th class="py-2 pr-4 font-medium">구분</th>
              <th class="py-2 pr-4 font-medium">거래명</th>
              <th class="py-2 pr-4 text-right font-medium">금액</th>
              <th class="py-2 pl-4 font-medium">관리</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in data.items"
              :key="item.ledgerNo"
              class="border-b border-[var(--color-border)] last:border-0"
            >
              <td class="py-2 pr-4">{{ item.trxDate }}</td>
              <td class="py-2 pr-4">{{ TRX_TYPE_LABEL[item.trxType] ?? item.trxType }}</td>
              <td class="py-2 pr-4">{{ item.trxName ?? '-' }}</td>
              <td class="py-2 pr-4 text-right">{{ formatCurrency(item.amount) }}</td>
              <td class="py-2 pl-4">
                <div class="flex gap-2">
                  <button
                    type="button"
                    class="text-[var(--color-primary)] hover:underline"
                    @click="openModal(item)"
                  >
                    수정
                  </button>
                  <button
                    type="button"
                    class="text-red-600 hover:underline"
                    @click="handleDelete(item)"
                  >
                    삭제
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>

    <Teleport to="body">
      <Transition name="modal-fade">
        <div
          v-if="isModalOpen"
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
          @keydown.esc="closeModal"
        >
          <div class="w-full max-w-sm rounded-lg bg-[var(--color-surface)] p-6 shadow-lg">
            <h2 class="text-base font-semibold text-[var(--color-text)]">
              {{ editingLedgerNo ? '장부 수정' : '장부 등록' }}
            </h2>

            <form class="mt-4 space-y-4" @submit.prevent="submitForm">
              <AppRadioGroup
                v-model="form.trxType"
                name="trxType"
                label="구분"
                :options="TRX_TYPE_OPTIONS"
              />
              <AppInput
                v-model="amountInput"
                type="number"
                label="금액"
                placeholder="0"
              />
              <AppDatePicker
                v-model="form.trxDate"
                label="거래일"
                :max="todayString()"
              />
              <AppInput
                v-model="form.trxName"
                label="거래명 (선택)"
                placeholder="예: 사과 매입"
              />

              <p v-if="formError" class="text-xs text-red-500">{{ formError }}</p>

              <div class="mt-2 flex justify-end gap-2">
                <AppButton type="button" variant="secondary" @click="closeModal">취소</AppButton>
                <AppButton type="submit" variant="primary" :loading="submitting">
                  {{ editingLedgerNo ? '수정' : '등록' }}
                </AppButton>
              </div>
            </form>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.15s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
</style>
