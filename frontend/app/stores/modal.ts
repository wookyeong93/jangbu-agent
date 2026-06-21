import { defineStore } from 'pinia'
import type { ModalOptions } from '~/types/modal'

const DEFAULT_OPTIONS: ModalOptions = { message: '' }

/**
 * 전역 모달 — 플래그(isOpen) 하나로 화면 어디서든 동일한 AppModal 인스턴스를 띄운다.
 * 페이지마다 v-model 로 로컬 상태를 따로 들지 않는다.
 *
 * 사용 예:
 * ```ts
 * const modal = useModalStore()
 * const ok = await modal.confirm({ title: '삭제', message: '정말 삭제하시겠습니까?', variant: 'danger' })
 * if (ok) { ... }
 * ```
 */
export const useModalStore = defineStore('modal', () => {
  const isOpen = ref(false)
  const options = ref<ModalOptions>(DEFAULT_OPTIONS)

  let resolver: ((result: boolean) => void) | null = null

  function open(opts: ModalOptions): Promise<boolean> {
    options.value = opts
    isOpen.value = true
    return new Promise((resolve) => {
      resolver = resolve
    })
  }

  function confirm(opts: Omit<ModalOptions, 'mode'>) {
    return open({ ...opts, mode: 'confirm' })
  }

  function alert(opts: Omit<ModalOptions, 'mode' | 'cancelText'>) {
    return open({ ...opts, mode: 'alert' })
  }

  function handleConfirm() {
    isOpen.value = false
    options.value.onConfirm?.()
    resolver?.(true)
    resolver = null
  }

  function handleCancel() {
    isOpen.value = false
    options.value.onCancel?.()
    resolver?.(false)
    resolver = null
  }

  return { isOpen, options, confirm, alert, handleConfirm, handleCancel }
})
