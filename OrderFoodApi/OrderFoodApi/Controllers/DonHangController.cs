﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using OrderFoodApi.Entity;
using Microsoft.EntityFrameworkCore;
using OrderFoodApi.Controllers.Shared;

// For more information on enabling Web API for empty projects, visit https://go.microsoft.com/fwlink/?LinkID=397860

namespace OrderFoodApi.Controllers
{
    [Route("[controller]/[action]")]
    public class DonHangController : Controller
    {
        readonly OrderFoodContext _db;
        public DonHangController(OrderFoodContext db)
        {
            _db = db;
        }

        [HttpPost]
        public async Task<IActionResult> DatHang([FromBody] DonHangData data)
        {
            if (data.ChiTietDonHangDatas.Count <= 0)
            {
                return BadRequest("Qua it");
            }
            else
            {
                var donHang = new DonHang()
                {
                    Sdt = data.Sdt,
                    TinhTrangDonHang = TinhTrangDonHang.ChoXuLy,
                    Ngay = DateTime.Now,
                    ChiTietDonHangs = new List<ChiTietDonHang>()
                };
                List<Task> tasks = new List<Task>();

                foreach (var ctdh in data.ChiTietDonHangDatas)
                {
                    if (ctdh.SoLuong <= 0)
                    {
                        return BadRequest("So luong phai lon hon 0");
                    }
                    else
                    {
                        var monAn = await GetMonAnById(ctdh.MonAnId);
                        if (monAn == null)
                        {
                            return BadRequest("Mon an khong ton tai");
                        }
                        // mon an ton tai
                        donHang.ChiTietDonHangs.Add(new ChiTietDonHang()
                        {
                            MonAnId = ctdh.MonAnId,
                            SoLuong = ctdh.SoLuong,
                            DonGia = monAn.Gia
                        });
                    }
                }

                await _db.DonHangs.AddAsync(donHang);
                await _db.SaveChangesAsync();

                return Json(donHang);
            }

        }

        [HttpGet]
        [Route("{id}")]
        public async Task<IActionResult> GetDonHang(int id)
        {
            return Json(await _db.DonHangs.Include(dh => dh.ChiTietDonHangs).FirstOrDefaultAsync(dh => dh.DonHangId == id));
        }

        [HttpGet]
        [Route("{id}")]
        public async Task<IActionResult> GetDonHangs(string id)
        {
            return Json(await _db.DonHangs.Include(dh => dh.ChiTietDonHangs).Where(dh => dh.Sdt == id).ToListAsync());
        }

        [HttpPost]
        public async Task<IActionResult> UpdateTinhTrangDonHang([FromBody] DonHangUpdateTinhTrangDonHangData data)
        {
            if (await _db.QuanLys.FirstOrDefaultAsync(ql => ql.QuanLyId == data.QuanLy.QuanLyId && ql.Password == data.QuanLy.Password) == null)
            {
                return Json(new ResponseData(1, null, "Không xác thực"));
            }
            var donHangInDb = await _db.DonHangs.FirstOrDefaultAsync(dh => dh.DonHangId == data.DonHangId);
            if (donHangInDb == null)
            {
                return Json(new ResponseData(2, null, "Đơn hàng không tồn tại"));
            }
            donHangInDb.TinhTrangDonHang = data.TinhTrangMoi;
            await _db.SaveChangesAsync();
            return Json(new ResponseData(0, donHangInDb));
        }

        [HttpGet]
        public async Task<IActionResult> ChoXuLy()
        {
            var donHangs = await _db.DonHangs
                .Include(dh => dh.ChiTietDonHangs)
                .Include(dh => dh.KhachHang)
                .Where(dh => dh.TinhTrangDonHang == TinhTrangDonHang.ChoXuLy)
                .ToListAsync();
            foreach (var dh in donHangs)
            {
                foreach (var ctdh in dh.ChiTietDonHangs)
                {
                    await _db.Entry(ctdh).Reference(m => m.MonAn).LoadAsync();
                }
            }
            return Json(new ResponseData(0, donHangs));
        }

        [HttpGet]
        public async Task<IActionResult> DangGiao()
        {
            var donHangs = await _db.DonHangs
                .Include(dh => dh.ChiTietDonHangs)
                .Include(dh => dh.KhachHang)
                .Where(dh => dh.TinhTrangDonHang == TinhTrangDonHang.DangGiaoHang)
                .ToListAsync();
            foreach (var dh in donHangs)
            {
                foreach (var ctdh in dh.ChiTietDonHangs)
                {
                    await _db.Entry(ctdh).Reference(m => m.MonAn).LoadAsync();
                }
            }
            return Json(new ResponseData(0, donHangs));
        }

        [HttpGet]
        public async Task<IActionResult> DaXuLy()
        {
            var donHangs = await _db.DonHangs
                .Include(dh => dh.ChiTietDonHangs)
                .Include(dh => dh.KhachHang)
                .Where(dh => dh.TinhTrangDonHang == TinhTrangDonHang.DaGiao || dh.TinhTrangDonHang == TinhTrangDonHang.DaHuy)
                .ToListAsync();
            foreach (var dh in donHangs)
            {
                foreach (var ctdh in dh.ChiTietDonHangs)
                {
                    await _db.Entry(ctdh).Reference(m => m.MonAn).LoadAsync();
                }
            }
            return Json(new ResponseData(0, donHangs));
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> Delete(int id)
        {
            var innerDonHang = await _db.DonHangs.FirstOrDefaultAsync(dh => dh.DonHangId == id);
            if (innerDonHang == null)
            {
                return Json(new ResponseData(1, null, "Đơn hàng không tồn tại"));
            }
            else
            {
                _db.DonHangs.Remove(innerDonHang);
                await _db.SaveChangesAsync();
                return Json(new ResponseData(0));
            }
        }

        private async Task<MonAn> GetMonAnById(int id)
        {
            return await _db.MonAns.FirstOrDefaultAsync(ma => ma.MonAnId == id);
        }

        public class DonHangData
        {
            public List<ChiTietDonHangData> ChiTietDonHangDatas { get; set; }
            public string Sdt { get; set; }
        }

        public class ChiTietDonHangData
        {
            public int MonAnId { get; set; }
            public int SoLuong { get; set; }
        }

        public class DonHangUpdateTinhTrangDonHangData
        {
            public QuanLy QuanLy { get; set; }
            public int DonHangId { get; set; }
            public TinhTrangDonHang TinhTrangMoi { get; set; }
        }
    }
}
