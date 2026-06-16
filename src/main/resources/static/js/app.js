document.addEventListener("DOMContentLoaded", () => {
    const csrfToken = document.querySelector("meta[name='csrf-token']")?.content;
    if (csrfToken) {
        document.querySelectorAll("form[method='post' i]").forEach((form) => {
            if (!form.querySelector("input[name='_csrf']")) {
                const input = document.createElement("input");
                input.type = "hidden";
                input.name = "_csrf";
                input.value = csrfToken;
                form.prepend(input);
            }
        });
    }

    document.querySelectorAll(".nav-dropdown").forEach((dropdown) => {
        let closeTimer;
        const open = () => {
            window.clearTimeout(closeTimer);
            dropdown.classList.add("is-open");
        };
        const close = () => {
            window.clearTimeout(closeTimer);
            closeTimer = window.setTimeout(() => dropdown.classList.remove("is-open"), 120);
        };
        dropdown.addEventListener("mouseenter", open);
        dropdown.addEventListener("mouseleave", close);
        dropdown.addEventListener("focusin", open);
        dropdown.addEventListener("focusout", (event) => {
            if (!dropdown.contains(event.relatedTarget)) {
                close();
            }
        });
    });

    document.querySelectorAll("[data-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const message = form.getAttribute("data-confirm");
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    });

    document.querySelectorAll("[data-carousel]").forEach((carousel) => {
        const slides = Array.from(carousel.querySelectorAll(".carousel-slide"));
        const dots = Array.from(carousel.querySelectorAll("[data-carousel-dot]"));
        const previous = carousel.querySelector("[data-carousel-prev]");
        const next = carousel.querySelector("[data-carousel-next]");
        let index = 0;
        let timerId;

        const show = (nextIndex) => {
            index = (nextIndex + slides.length) % slides.length;
            const prevIndex = (index - 1 + slides.length) % slides.length;
            const nextIndexValue = (index + 1) % slides.length;
            slides.forEach((slide, slideIndex) => {
                slide.classList.toggle("is-active", slideIndex === index);
                slide.classList.toggle("is-prev", slideIndex === prevIndex);
                slide.classList.toggle("is-next", slideIndex === nextIndexValue);
                slide.classList.toggle("is-far", slideIndex !== index && slideIndex !== prevIndex && slideIndex !== nextIndexValue);
            });
            dots.forEach((dot, dotIndex) => {
                dot.classList.toggle("is-active", dotIndex === index);
            });
        };

        const start = () => {
            timerId = window.setInterval(() => show(index + 1), 4000);
        };

        const restart = () => {
            window.clearInterval(timerId);
            start();
        };

        previous?.addEventListener("click", () => {
            show(index - 1);
            restart();
        });
        next?.addEventListener("click", () => {
            show(index + 1);
            restart();
        });
        dots.forEach((dot) => {
            dot.addEventListener("click", () => {
                show(Number(dot.dataset.carouselDot));
                restart();
            });
        });
        carousel.addEventListener("mouseenter", () => window.clearInterval(timerId));
        carousel.addEventListener("mouseleave", start);
        show(0);
        start();
    });

    document.querySelectorAll("[data-cover-input]").forEach((input) => {
        input.addEventListener("change", () => {
            const file = input.files?.[0];
            if (!file) {
                return;
            }
            const form = input.closest("form");
            const previewWrap = form?.querySelector(".cover-preview");
            const empty = previewWrap?.querySelector("[data-cover-empty]");
            let image = previewWrap?.querySelector("[data-cover-preview]");
            if (!image && previewWrap) {
                image = document.createElement("img");
                image.setAttribute("data-cover-preview", "");
                image.alt = "灏侀潰棰勮";
                previewWrap.prepend(image);
            }
            if (image) {
                image.src = URL.createObjectURL(file);
            }
            empty?.classList.add("is-hidden");
        });
    });

    document.querySelectorAll("[data-contact-toggle]").forEach((button) => {
        button.addEventListener("click", () => {
            const box = document.querySelector("[data-contact-box]");
            if (box) {
                box.hidden = !box.hidden;
            }
        });
    });

    document.querySelectorAll(".flash").forEach((flash) => {
        window.setTimeout(() => flash.remove(), 2100);
    });

    document.querySelectorAll("[data-image-zoom]").forEach((image) => {
        image.addEventListener("mousemove", (event) => {
            const rect = image.getBoundingClientRect();
            const x = ((event.clientX - rect.left) / rect.width) * 100;
            const y = ((event.clientY - rect.top) / rect.height) * 100;
            image.style.transformOrigin = `${x}% ${y}%`;
        });
        image.addEventListener("mouseleave", () => {
            image.style.transformOrigin = "center";
        });
    });

    document.querySelectorAll("[data-purchase-quantity-source]").forEach((form) => {
        const input = form.querySelector("[data-purchase-quantity]");
        const detailPanel = form.closest(".panel");
        const hiddenQuantity = detailPanel?.querySelector("[data-buy-now-quantity]");
        if (!input || !hiddenQuantity) {
            return;
        }
        const sync = () => {
            hiddenQuantity.value = input.value || "1";
        };
        input.addEventListener("input", sync);
        input.addEventListener("change", sync);
        sync();
    });

    document.querySelectorAll("[data-checkout-form]").forEach((form) => {
        const originalAmount = Number(form.dataset.originalAmount || 0);
        const availableCoins = Number(form.dataset.coins || 0);
        const radios = Array.from(form.querySelectorAll("input[name='discountType']"));
        const couponSelect = form.querySelector("select[name='couponId']");
        const paymentSelect = form.querySelector("select[name='paymentMethod']");
        const wechatToken = form.querySelector("[data-wechat-token]");
        const wechatModal = form.querySelector("[data-wechat-modal]");
        const wechatCloseButtons = Array.from(form.querySelectorAll("[data-wechat-close]"));
        const wechatQr = form.querySelector("[data-wechat-qr]");
        const wechatStatus = form.querySelector("[data-wechat-status]");
        const wechatLink = form.querySelector("[data-wechat-link]");
        const discountDisplay = form.querySelector("[data-discount-display]");
        const payableDisplays = Array.from(form.querySelectorAll("[data-payable-display]"));
        const hint = form.querySelector("[data-discount-hint]");
        const currency = (value) => `¥${Math.max(0, value).toFixed(2)}`;

        const openWechatModal = () => {
            if (!wechatModal) {
                return;
            }
            wechatModal.hidden = false;
            document.body.classList.add("modal-open");
        };

        const closeWechatModal = () => {
            if (!wechatModal) {
                return;
            }
            window.clearInterval(wechatPollTimer);
            wechatModal.hidden = true;
            document.body.classList.remove("modal-open");
        };

        const createWechatSession = async () => {
            const token = document.querySelector("meta[name='csrf-token']")?.content || "";
            const payableText = payableDisplays[0]?.textContent?.trim() || currency(originalAmount);
            const response = await fetch("/wechat-pay/sessions", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
                body: new URLSearchParams({
                    amount: payableText,
                    _csrf: token,
                }),
            });
            if (!response.ok) {
                throw new Error("create wechat session failed");
            }
            return response.json();
        };

        const pollWechatStatus = (token) => {
            window.clearInterval(wechatPollTimer);
            wechatPollTimer = window.setInterval(async () => {
                try {
                    const response = await fetch(`/wechat-pay/status/${encodeURIComponent(token)}`);
                    const status = await response.json();
                    if (!status.valid) {
                        window.clearInterval(wechatPollTimer);
                        if (wechatStatus) {
                            wechatStatus.textContent = "QR code expired. Close and try again.";
                        }
                        return;
                    }
                    if (status.confirmed) {
                        window.clearInterval(wechatPollTimer);
                        if (wechatStatus) {
                            wechatStatus.textContent = "Confirmed on phone. Submitting order...";
                        }
                        wechatSubmitting = true;
                        form.requestSubmit();
                    }
                } catch (error) {
                    if (wechatStatus) {
                        wechatStatus.textContent = "Waiting for phone confirmation. Keep this page open.";
                    }
                }
            }, 1200);
        };

        let wechatPollTimer;
        let wechatSubmitting = false;

        const calculate = () => {
            const selectedType = form.querySelector("input[name='discountType']:checked")?.value || "";
            let discount = 0;
            let message = "No discount selected.";

            if (selectedType === "COINS") {
                const maxUsableCoins = Math.floor(originalAmount * 100);
                const usableCoins = Math.min(Math.floor(availableCoins / 100) * 100, maxUsableCoins);
                discount = usableCoins / 100;
                message = usableCoins > 0
                    ? `Using ${usableCoins} coins, discount ${currency(discount)}.`
                    : "Coins are less than 100 and cannot be used.";
            }

            if (selectedType === "COUPON") {
                if (couponSelect && !couponSelect.value && couponSelect.options.length > 1) {
                    couponSelect.selectedIndex = 1;
                }
                const option = couponSelect?.selectedOptions?.[0];
                if (!option || !option.value) {
                    message = "Select a coupon to preview the payable amount.";
                } else if (option.dataset.type === "AMOUNT_OFF") {
                    const threshold = Number(option.dataset.threshold || 0);
                    const reduce = Number(option.dataset.reduce || 0);
                    if (originalAmount >= threshold) {
                        discount = Math.min(reduce, originalAmount);
                        message = `Amount-off coupon applied, discount ${currency(discount)}.`;
                    } else {
                        message = `Current amount is below ${currency(threshold)}.`;
                    }
                } else {
                    const rate = Number(option.dataset.rate || 0.9);
                    discount = originalAmount * (1 - rate);
                    message = `Discount coupon applied, discount ${currency(discount)}.`;
                }
            }

            discount = Math.max(0, Math.min(originalAmount, discount));
            if (discountDisplay) discountDisplay.textContent = `-${currency(discount)}`;
            payableDisplays.forEach((payableDisplay) => {
                payableDisplay.textContent = currency(originalAmount - discount);
            });
            if (hint) hint.textContent = message;
        };

        radios.forEach((radio) => radio.addEventListener("change", () => {
            if (radio.value === "COUPON" && radio.checked && couponSelect && !couponSelect.value && couponSelect.options.length > 1) {
                couponSelect.selectedIndex = 1;
            }
            calculate();
        }));
        form.addEventListener("input", (event) => {
            if (event.target?.matches?.("input[name='discountType']")) {
                calculate();
            }
        });
        couponSelect?.addEventListener("change", () => {
            const couponRadio = form.querySelector("input[name='discountType'][value='COUPON']");
            if (couponRadio && couponSelect.value) {
                couponRadio.checked = true;
            }
            calculate();
        });
        const checkedRadio = form.querySelector("input[name='discountType']:checked");
        if (checkedRadio?.value === "COUPON" && couponSelect && !couponSelect.value && couponSelect.options.length > 1) {
            couponSelect.selectedIndex = 1;
        }
        paymentSelect?.addEventListener("change", () => {
            if (wechatToken && paymentSelect.value !== "WECHAT") {
                wechatToken.value = "";
            }
        });
        form.addEventListener("submit", async (event) => {
            if (paymentSelect?.value === "WECHAT" && !wechatSubmitting) {
                event.preventDefault();
                openWechatModal();
                if (wechatStatus) {
                    wechatStatus.textContent = "Generating scannable QR code...";
                }
                try {
                    const session = await createWechatSession();
                    if (wechatToken) {
                        wechatToken.value = session.token;
                    }
                    if (window.qrcode && wechatQr) {
                        const qr = window.qrcode(0, "M");
                        qr.addData(session.confirmUrl);
                        qr.make();
                        wechatQr.innerHTML = qr.createSvgTag(5, 4, "微信扫码确认购买");
                    }
                    if (wechatStatus) {
                        wechatStatus.textContent = "Scan with WeChat, then confirm purchase on your phone.";
                    }
                    if (wechatLink) {
                        wechatLink.textContent = session.confirmUrl;
                    }
                    pollWechatStatus(session.token);
                } catch (error) {
                    if (wechatStatus) {
                        wechatStatus.textContent = "QR code generation failed. Make sure the local app is running.";
                    }
                }
            }
        });
        wechatCloseButtons.forEach((button) => {
            button.addEventListener("click", () => {
                if (wechatToken) {
                    wechatToken.value = "";
                }
                wechatSubmitting = false;
                closeWechatModal();
            });
        });
        calculate();
    });

    // Reward form: dynamically show/hide fields based on reward & coupon type
    document.querySelectorAll("form").forEach((form) => {
        const rewardSelect = form.querySelector("[data-reward-type]");
        const couponSelect = form.querySelector("[data-coupon-type]");
        if (!rewardSelect && !couponSelect) {
            return;
        }
        const apply = () => {
            const reward = rewardSelect ? rewardSelect.value : "COUPON";
            const coupon = couponSelect ? couponSelect.value : "DISCOUNT_RATE";
            form.querySelectorAll("[data-coin-only]").forEach((el) => {
                el.style.display = reward === "COIN" ? "" : "none";
            });
            form.querySelectorAll("[data-coupon-only]").forEach((el) => {
                el.style.display = reward === "COUPON" ? "" : "none";
            });
            form.querySelectorAll("[data-discount-rate]").forEach((el) => {
                el.style.display = (reward === "COUPON" && coupon === "DISCOUNT_RATE") ? "" : "none";
            });
            form.querySelectorAll("[data-amount-off]").forEach((el) => {
                el.style.display = (reward === "COUPON" && coupon === "AMOUNT_OFF") ? "" : "none";
            });
        };
        rewardSelect?.addEventListener("change", apply);
        couponSelect?.addEventListener("change", apply);
        apply();
    });

    // Mail broadcast toggle: hides target-username when broadcasting
    document.querySelectorAll("[data-broadcast-toggle]").forEach((checkbox) => {
        const form = checkbox.closest("form");
        if (!form) return;
        const target = form.querySelector("[data-target-username]");
        const apply = () => {
            if (!target) return;
            target.style.display = checkbox.checked ? "none" : "";
            const input = target.querySelector("input");
            if (input) input.required = !checkbox.checked;
        };
        checkbox.addEventListener("change", apply);
        apply();
    });

    // Flash sale countdown
    document.querySelectorAll("[data-countdown]").forEach((card) => {
        const target = Number(card.getAttribute("data-countdown"));
        if (!target || Number.isNaN(target)) return;
        const display = card.querySelector("[data-countdown-display]");
        if (!display) return;
        const cellD = display.querySelector("[data-d]");
        const cellH = display.querySelector("[data-h]");
        const cellM = display.querySelector("[data-m]");
        const cellS = display.querySelector("[data-s]");
        const pad = (n) => String(Math.max(0, n)).padStart(2, "0");
        const tick = () => {
            const remain = target - Date.now();
            if (remain <= 0) {
                cellD.textContent = "00";
                cellH.textContent = "00";
                cellM.textContent = "00";
                cellS.textContent = "00";
                return false;
            }
            const seconds = Math.floor(remain / 1000);
            cellD.textContent = pad(Math.floor(seconds / 86400));
            cellH.textContent = pad(Math.floor((seconds % 86400) / 3600));
            cellM.textContent = pad(Math.floor((seconds % 3600) / 60));
            cellS.textContent = pad(seconds % 60);
            return true;
        };
        if (tick()) {
            const handle = window.setInterval(() => {
                if (!tick()) window.clearInterval(handle);
            }, 1000);
        }
    });

    document.querySelectorAll("form").forEach((form) => {
        form.addEventListener("submit", () => {
            if (form.dataset.confirm || form.dataset.checkoutForm !== undefined) {
                return;
            }
            const submitter = form.querySelector("button[type='submit'], button:not([type])");
            if (submitter && !submitter.disabled) {
                submitter.classList.add("is-loading");
                submitter.setAttribute("aria-busy", "true");
            }
        });
    });
});
